package com.bankapp.accountservice.repository;

import com.bankapp.accountservice.model.OutboxEvent;
import com.bankapp.accountservice.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, java.util.UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
