package com.abiz.config;

import java.math.BigDecimal;

public final class AppConstants {

    public static final String TABLE_PREFIX = "interview_";
    public static final BigDecimal MAX_WALLET = new BigDecimal(1_000_000);
    public static final BigDecimal MAX_REQUEST_AMOUNT = new BigDecimal(1_000);
    public static final String USER_LOCK_PREFIX = "user_";
    public static final String WALLET_PREFIX = "wallet_";
    public static final String API_LOCK_PREFIX = "api_";


    private AppConstants() {
    }
}
