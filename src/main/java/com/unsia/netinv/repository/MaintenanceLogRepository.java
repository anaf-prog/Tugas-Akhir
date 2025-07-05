package com.unsia.netinv.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.MaintenanceLog;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long>, JpaSpecificationExecutor<MaintenanceLog>{

    @Query("SELECT ml FROM MaintenanceLog ml LEFT JOIN FETCH ml.device ORDER BY ml.maintenanceDate DESC")
    List<MaintenanceLog> findAllWithDevice();

    @Query("SELECT m FROM MaintenanceLog m JOIN FETCH m.device ORDER BY m.maintenanceDate DESC")
    List<MaintenanceLog> findAllWithDeviceOrderedByMaintenanceDateDesc();

    List<MaintenanceLog> findByScheduledTimeAfterAndAutoDisable(LocalDateTime now, boolean b);

    @Query("SELECT ml FROM MaintenanceLog ml LEFT JOIN FETCH ml.device ORDER BY ml.maintenanceDate DESC")
    Page<MaintenanceLog> findAllWithDeviceOrderedByMaintenanceDateDesc(Pageable pageable);

    // @Query("SELECT ml FROM MaintenanceLog ml LEFT JOIN FETCH ml.device WHERE " +
    //        "LOWER(ml.device.deviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
    //        "LOWER(ml.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
    //        "LOWER(ml.technician) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
    //        "ORDER BY ml.maintenanceDate DESC")
    // Page<MaintenanceLog> searchMaintenanceLogs(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT ml FROM MaintenanceLog ml LEFT JOIN FETCH ml.device d WHERE " +
       "(LOWER(d.deviceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
       "LOWER(ml.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
       "LOWER(ml.technician) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
       "ORDER BY ml.maintenanceDate DESC")
    Page<MaintenanceLog> searchMaintenanceLogs(@Param("searchTerm") String searchTerm, Pageable pageable);
}
