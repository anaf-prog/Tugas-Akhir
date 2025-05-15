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

@Repository
public interface BackupRouteRepository extends JpaRepository<BackupRoutes, Long>, JpaSpecificationExecutor<BackupRoutes> {
    Optional<BackupRoutes> findByMainDeviceId(Long mainDeviceId);

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
}
