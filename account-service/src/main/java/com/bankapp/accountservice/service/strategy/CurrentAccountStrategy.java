package com.bankapp.accountservice.service.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CurrentAccountStrategy implements AccountTypeStrategy {

    @Override
    public BigDecimal minimumBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal overdraftFee() {
        return new BigDecimal("25.00"); // flat overdraft fee instead of a hard reject
    }
}
