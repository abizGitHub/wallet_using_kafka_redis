package com.abiz.service;

import com.abiz.util.LockService;
import com.abiz.config.AppConstants;
import com.abiz.controller.dto.FinancialDto;
import com.abiz.controller.dto.WalletDto;
import com.abiz.domain.enumeration.AccountingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class WalletService implements IWalletService, IWalletConsumer {

    private final LockService lockService;

    public WalletService(LockService lockService) {
        this.lockService = lockService;
    }

    private void addToWallet(String user, BigDecimal amount) {
        if (amount.compareTo(AppConstants.MAX_REQUEST_AMOUNT) > 0) {
            return; // TODO add exception or log an event & release lock
        }
        BigDecimal pre = lockService.acquireLockAndGetCredit(user);
        BigDecimal newAmount = pre.add(amount);
        if (newAmount.compareTo(AppConstants.MAX_WALLET) > 0) {
            lockService.releaseLock(user);
            return; // TODO add exception or log an event & release lock
        }
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            lockService.releaseLock(user);
            return; // TODO add exception or log an event & release lock
        }
        lockService.setCreditAndReleaseLock(user, newAmount);
    }

    @Override
    public WalletDto getWallet(String user) {
        BigDecimal credit = lockService.getCredit(user);
        return WalletDto.builder().credit(credit).user(user).build();
    }

    @Override
    public void apply(String requestId, FinancialDto dto) {
        if (dto.getStatus().equals(AccountingStatus.DEBTOR)) {
            addToWallet(dto.getUser(), dto.getAmount().negate());
        } else if (dto.getStatus().equals(AccountingStatus.CREDITOR)) {
            addToWallet(dto.getUser(), dto.getAmount());
        }
        if (requestId != null) {
            lockService.notifyRequest(requestId);
        }
    }
}
