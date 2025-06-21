package com.unsia.netinv.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.MaintenanceLog;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long>, JpaSpecificationExecutor<MaintenanceLog>{

    @Query("SELECT ml FROM MaintenanceLog ml LEFT JOIN FETCH ml.device ORDER BY ml.maintenanceDate DESC")
    List<MaintenanceLog> findAllWithDevice();

    @Query("SELECT m FROM MaintenanceLog m JOIN FETCH m.device ORDER BY m.maintenanceDate DESC")
    List<MaintenanceLog> findAllWithDeviceOrderedByMaintenanceDateDesc();



    List<MaintenanceLog> findByScheduledTimeAfterAndAutoDisable(LocalDateTime now, boolean b);
}
