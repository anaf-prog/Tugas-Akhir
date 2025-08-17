package com.unsia.netinv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.BackupRoutes;
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;

@Repository
public interface BackupRouteRepository extends JpaRepository<BackupRoutes, Long>, JpaSpecificationExecutor<BackupRoutes> {
    // Optional<BackupRoutes> findByMainDeviceId(Long mainDeviceId);

    // Query untuk mencari backup route berdasarkan main device (hasil unik)
    @Query("SELECT br FROM BackupRoutes br WHERE br.mainDevice.id = :mainDeviceId")
    Optional<BackupRoutes> findByMainDeviceId(@Param("mainDeviceId") Long mainDeviceId);

    // Query untuk mencari backup route berdasarkan backup device (bisa banyak hasil)
    @Query("SELECT br FROM BackupRoutes br WHERE br.backupDevice.id = :backupDeviceId")
    List<BackupRoutes> findAllByBackupDeviceId(@Param("backupDeviceId") Long backupDeviceId);

    // Query untuk mengecek apakah backup device aktif untuk main device tertentu
    @Query("SELECT COUNT(br) > 0 FROM BackupRoutes br WHERE br.backupDevice.id = :backupDeviceId AND br.mainDevice.id = :mainDeviceId")
    boolean isBackupForMainDevice(@Param("backupDeviceId") Long backupDeviceId, @Param("mainDeviceId") Long mainDeviceId);

    @Query("SELECT COUNT(br) > 0 FROM BackupRoutes br WHERE br.backupDevice.id = :deviceId AND br.isActive = true")
    boolean isActiveBackup(@Param("deviceId") Long deviceId);
    
    @Query("SELECT br.mainDevice FROM BackupRoutes br WHERE br.backupDevice.id = :backupDeviceId AND br.isActive = true")
    Optional<Device> findActiveMainDevice(@Param("backupDeviceId") Long backupDeviceId);

    // Cek apakah device adalah backup yang aktif
    boolean existsByBackupDeviceIdAndIsActive(Long backupDeviceId, Boolean isActive);

    List<BackupRoutes> findByMainDevice(Device device);

    // void deleteByMainDevice(Device mainDevice);

    @Modifying
    @Query("DELETE FROM BackupRoutes br WHERE br.mainDevice = :mainDevice")
    void deleteByMainDevice(@Param("mainDevice") Device mainDevice);

    @Query("SELECT CASE WHEN COUNT(br) > 0 THEN true ELSE false END FROM BackupRoutes br WHERE br.backupDevice.id = :deviceId")
    boolean existsByBackupDeviceId(@Param("deviceId") Long deviceId);

    // Tambahkan method baru
    // Optional<BackupRoutes> findByBackupDeviceId(Long backupDeviceId);

    @Query("SELECT br FROM BackupRoutes br WHERE br.backupDevice.id = :backupDeviceId")
    Optional<BackupRoutes> findByBackupDeviceId(@Param("backupDeviceId") Long backupDeviceId);
    
    // Tambahkan method untuk mencari failover aktif
    @Query("SELECT fl FROM FailOverLogs fl WHERE fl.mainDevice.id = :mainDeviceId AND fl.repairTime IS NULL")
    List<FailOverLogs> findByMainDeviceIdAndRepairTimeIsNull(@Param("mainDeviceId") Long mainDeviceId);
}