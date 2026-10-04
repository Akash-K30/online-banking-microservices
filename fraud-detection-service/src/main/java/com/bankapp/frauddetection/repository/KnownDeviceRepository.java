package com.bankapp.frauddetection.repository;

import com.bankapp.frauddetection.entity.KnownDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KnownDeviceRepository extends JpaRepository<KnownDevice, UUID> {

    Optional<KnownDevice> findByAccountIdAndDeviceIdAndLocation(UUID accountId, String deviceId, String location);

    boolean existsByAccountIdAndDeviceIdAndLocation(UUID accountId, String deviceId, String location);
}
