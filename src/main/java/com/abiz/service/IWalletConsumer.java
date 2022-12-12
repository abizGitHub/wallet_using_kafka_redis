package com.abiz.service;

import com.abiz.controller.dto.FinancialDto;

public interface IWalletConsumer {

    void apply(String requestId, FinancialDto dto);
}
