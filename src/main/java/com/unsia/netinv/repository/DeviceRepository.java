package com.unsia.netinv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.Device;

import jakarta.persistence.LockModeType;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device>{
    List<Device> findByStatusDevice(String status);

    Optional<Device> findById(Long Id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Device d WHERE d.id = :id")
    Optional<Device> findByIdWithLock(@Param("id") Long id);

    long countByStatusDevice(String status);

    boolean existsByIpAddress(String ipAddress);

    Device findByIpAddress(String ipAddress);

    long countByDeviceType(String deviceType);
    
}
