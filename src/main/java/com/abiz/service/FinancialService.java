package com.abiz.service;

import com.abiz.domain.FinancialEntity;
import com.abiz.kafka.Producer;
import com.abiz.util.LockService;
import com.abiz.controller.dto.FinancialDto;
import com.abiz.controller.dto.WalletDto;
import com.abiz.controller.mapper.FinancialMapper;
import com.abiz.controller.model.FinancialModel;
import com.abiz.domain.enumeration.AccountingStatus;
import com.abiz.exception.DataFormatException;
import com.abiz.exception.EntityNotFoundException;
import com.abiz.repository.FinancialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinancialService implements IFinancialService {

    private final FinancialRepository repository;
    private final FinancialMapper mapper;
    private final Producer producer;
    private final LockService lockService;
    private final IWalletService walletService;

    public FinancialService(FinancialRepository repository, FinancialMapper mapper, Producer producer, LockService lockService, IWalletService walletService) {
        this.repository = repository;
        this.mapper = mapper;
        this.producer = producer;
        this.lockService = lockService;
        this.walletService = walletService;
    }

    @Override
    public FinancialDto save(FinancialModel model) {
        FinancialEntity entity = mapper.toEntity(model);
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public List<FinancialDto> findAll() {
        return mapper.toDto(repository.findAll());
    }

    @Override
    public FinancialDto update(FinancialModel model) {
        FinancialEntity entity = mapper.toEntity(model);
        boolean exists = repository.existsById(entity.getId());
        if (!exists) {
            throw new EntityNotFoundException(entity.getId());
        }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public FinancialDto partialUpdate(String id, String key, Object value) {
        boolean exists = repository.existsById(id);
        if (!exists) {
            throw new EntityNotFoundException(id);
        }
        FinancialEntity entity = repository.getById(id);
        switch (key) {
            case "status" -> {
                entity.setStatus(AccountingStatus.valueOf((String) value));
                break;
            }
            case "user" -> {
                entity.setUser((String) value);
                break;
            }
            case "amount" -> {
                if (value instanceof String s) {
                    entity.setAmount(new BigDecimal(s));
                } else if (value instanceof Double) {
                    entity.setAmount(BigDecimal.valueOf((Double) value));
                } else if (value instanceof Integer) {
                    entity.setAmount(BigDecimal.valueOf((Integer) value));
                } else {
                    entity.setAmount(BigDecimal.valueOf((Long) value));
                }
                break;
            }
            default -> {
                throw new DataFormatException();
            }
        }
        entity = repository.save(entity);
        return mapper.toDto(entity);
    }

    @Override
    public void deleteById(String id) {
        FinancialEntity entity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
        repository.delete(entity);
    }

    @Override
    public List<FinancialDto> getByUser(String user) {
        List<FinancialEntity> list = repository.findAllByUser(user);
        return mapper.toDto(list);
    }

    @Override
    public FinancialDto getById(String id) {
        FinancialEntity entity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(id));
        return mapper.toDto(entity);
    }

    @Override
    public List<WalletDto> applyToWallet(List<FinancialDto> financialDtoList) throws InterruptedException {
        String requestId = UUID.randomUUID().toString();
        Map<String, List<FinancialDto>> map = financialDtoList.stream().collect(Collectors.groupingBy(FinancialDto::getUser));
        List<FinancialDto> list2Sent = new ArrayList<>();
        for (String user : map.keySet()) {
            list2Sent.addAll(refineAndApplyForUser(map.get(user)));
        }
        lockService.createApiRequestLock(requestId, list2Sent.size());
        producer.send(list2Sent, requestId);
        lockService.waitForApiRequest(requestId);
        List<WalletDto> list = new ArrayList<>();
        for (String user : map.keySet()) {
            list.add(walletService.getWallet(user));
        }
        //TODO whether is it required to persist FinancialEntity in wallet-service or here?
        // (persistence in DB after queue might reduce speed)
        return list;
    }

    private List<FinancialDto> refineAndApplyForUser(List<FinancialDto> financialDtoList) {
        List<FinancialDto> list = new ArrayList<>();
        AccountingStatus lastStatus = null;
        for (FinancialDto dto : financialDtoList) {
            if (dto.getStatus() != lastStatus) {
                lastStatus = dto.getStatus();
                list.add(dto);
            }
        }
        return list;
    }

}
