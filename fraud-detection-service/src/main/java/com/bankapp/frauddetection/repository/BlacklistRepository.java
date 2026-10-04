package com.bankapp.frauddetection.repository;

import com.bankapp.frauddetection.entity.BlacklistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistRepository extends JpaRepository<BlacklistEntry, java.util.UUID> {

    boolean existsByTypeAndValue(BlacklistEntry.Type type, String value);
}
