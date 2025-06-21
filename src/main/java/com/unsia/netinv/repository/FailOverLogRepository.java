package com.unsia.netinv.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;

@Repository
public interface FailOverLogRepository extends JpaRepository<FailOverLogs, Long>, JpaSpecificationExecutor<FailOverLogs> {

    Page<FailOverLogs> findAllByOrderByWaktuDesc(Pageable pageable);

    List<FailOverLogs> findByMainDeviceAndRepairTimeIsNull(Device device);

    List<FailOverLogs> findTopByMainDeviceAndRepairTimeBefore(Device device, LocalDateTime repairDate);

    Optional<FailOverLogs> findTopByMainDeviceAndRepairTimeIsNullOrderByWaktuDesc(Device device);

    List<FailOverLogs> findByMainDeviceAndRepairTimeIsNullAndWaktuBefore(Device device, LocalDateTime repairDate);
}
