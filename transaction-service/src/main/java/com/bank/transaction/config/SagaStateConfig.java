package com.bank.transaction.config;

import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.saga.state.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SagaStateConfig {


    @Bean
    public Map<TransactionStatus, TransactionState> transactionStates(InitiatedState initiatedState,
                                                                        FraudCheckState fraudCheckState,
                                                                        HoldPlacedState holdPlacedState,
                                                                        DebitedState debitedState,
                                                                        CompletedState completedState,
                                                                        FailedState failedState) {
        return Map.of(
                TransactionStatus.INITIATED, initiatedState,
                TransactionStatus.FRAUD_CHECK, fraudCheckState,
                TransactionStatus.HOLD_PLACED, holdPlacedState,
                TransactionStatus.DEBITED, debitedState,
                TransactionStatus.COMPLETED, completedState,
                TransactionStatus.FAILED, failedState
        );
    }
}
