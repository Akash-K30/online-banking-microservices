package com.bank.notification.repository;

import com.bank.notification.model.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserContactRepository extends JpaRepository<UserContact, UUID> {
}
