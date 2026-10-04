package com.bank.transaction.repository;

import com.bank.transaction.entity.AccountOwnerCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountOwnerCacheRepository extends JpaRepository<AccountOwnerCache, UUID> {
}
