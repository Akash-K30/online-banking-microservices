package com.bankapp.accountservice.service.strategy;

import java.math.BigDecimal;


public interface AccountTypeStrategy {

    /** Minimum balance this account type must retain after a debit. */
    BigDecimal minimumBalance();

    /** Fee applied to a debit that would breach the minimum balance rule, if any. */
    BigDecimal overdraftFee();
}
