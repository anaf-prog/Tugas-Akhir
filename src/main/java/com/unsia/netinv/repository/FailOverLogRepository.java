package com.unsia.netinv.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;

@Repository
public interface FailOverLogRepository extends JpaRepository<FailOverLogs, Long>, JpaSpecificationExecutor<FailOverLogs> {

    Page<FailOverLogs> findAllByOrderByWaktuDesc(Pageable pageable);

    List<FailOverLogs> findByMainDeviceAndRepairTimeIsNull(Device device);

    @Query("SELECT f FROM FailOverLogs f WHERE f.mainDevice = :mainDevice AND f.repairTime IS NULL ORDER BY f.waktu DESC")
    List<FailOverLogs> findByMainDeviceAndRepairTimeIsNullOrderByWaktuDesc(@Param("mainDevice") Device mainDevice);

    List<FailOverLogs> findTopByMainDeviceAndRepairTimeBefore(Device device, LocalDateTime repairDate);

    Optional<FailOverLogs> findTopByMainDeviceAndRepairTimeIsNullOrderByWaktuDesc(Device device);

    List<FailOverLogs> findByMainDeviceAndRepairTimeIsNullAndWaktuBefore(Device device, LocalDateTime repairDate);

    boolean existsByBackupDeviceIdAndRepairTimeIsNull(Long backupDeviceId); 

    @Query("SELECT fl FROM FailOverLogs fl WHERE fl.mainDevice.id = :mainDeviceId AND fl.repairTime IS NULL")
    List<FailOverLogs> findByMainDeviceIdAndRepairTimeIsNull(@Param("mainDeviceId") Long mainDeviceId);

    // Tambahkan method baru
    @Query("SELECT COUNT(f) > 0 FROM FailOverLogs f WHERE f.mainDevice = :mainDevice AND f.backupDevice = :backupDevice AND f.repairTime IS NULL")
    boolean existsActiveFailover(@Param("mainDevice") Device mainDevice, @Param("backupDevice") Device backupDevice);
    
}
