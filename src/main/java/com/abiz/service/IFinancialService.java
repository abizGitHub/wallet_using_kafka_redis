package com.abiz.service;


import com.abiz.controller.dto.FinancialDto;
import com.abiz.controller.dto.WalletDto;
import com.abiz.controller.model.FinancialModel;

import java.util.List;

public interface IFinancialService {


    FinancialDto save(FinancialModel model);

    List<FinancialDto> findAll();

    FinancialDto update(FinancialModel model);

    FinancialDto partialUpdate(String id, String key, Object value);

    void deleteById(String id);

    List<FinancialDto> getByUser(String user);

    FinancialDto getById(String id);

    List<WalletDto> applyToWallet(List<FinancialDto> financialDtoList) throws InterruptedException;
}
