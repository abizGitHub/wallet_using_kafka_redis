package com.abiz.service;

import com.abiz.controller.dto.WalletDto;


public interface IWalletService {
    WalletDto getWallet(String user);
}
