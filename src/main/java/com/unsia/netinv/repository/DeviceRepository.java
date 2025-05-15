package com.unsia.netinv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.Device;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device>{
    List<Device> findByStatusDevice(String status);

    Optional<Device> findById(Long Id);

    long countByStatusDevice(String status);

    boolean existsByIpAddress(String ipAddress);

    Device findByIpAddress(String ipAddress);
    
}
