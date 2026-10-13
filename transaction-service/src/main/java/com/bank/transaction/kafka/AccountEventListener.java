package com.bank.transaction.kafka;

import com.bank.transaction.dto.AccountEventMessage;
import com.bank.transaction.dto.AccountEventType;
import com.bank.transaction.entity.AccountOwnerCache;
import com.bank.transaction.repository.AccountOwnerCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AccountEventListener {

    private static final Logger log = LoggerFactory.getLogger(AccountEventListener.class);

    private final AccountOwnerCacheRepository ownerCacheRepository;

    public AccountEventListener(AccountOwnerCacheRepository ownerCacheRepository) {
        this.ownerCacheRepository = ownerCacheRepository;
    }

    @KafkaListener(
            topics = "account.events",
            groupId = "transaction-service",
            properties = {
                    "spring.json.use.type.headers=false",
                    "spring.json.value.default.type=com.bank.transaction.dto.AccountEventMessage"

            })
    public void onAccountEvent(AccountEventMessage message) {

        if (message.entryType() == AccountEventType.ACCOUNT_OPENED && message.userId() != null) {
            ownerCacheRepository.save(new AccountOwnerCache(message.accountId(), message.userId()));
        } else {
            log.debug("Ignoring account event {} for account {}", message.entryType(), message.accountId());
        }
    }
}
