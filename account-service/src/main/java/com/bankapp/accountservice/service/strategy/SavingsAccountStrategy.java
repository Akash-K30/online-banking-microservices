package com.bankapp.accountservice.service.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SavingsAccountStrategy implements AccountTypeStrategy {

    @Override
    public BigDecimal minimumBalance() {
        return new BigDecimal("500.00");
    }

    @Override
    public BigDecimal overdraftFee() {
        return BigDecimal.ZERO; // savings accounts simply reject the debit instead of charging a fee
    }
}
