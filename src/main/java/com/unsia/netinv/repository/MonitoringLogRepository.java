package com.unsia.netinv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.netinve.LogReason;

@Repository
public interface MonitoringLogRepository extends JpaRepository<MonitoringLog, Long>, JpaSpecificationExecutor<MonitoringLog> {

    List<MonitoringLog> findTop10ByOrderByMonitoringDesc();

    @Query("SELECT ml FROM MonitoringLog ml WHERE ml.monitoring IN (SELECT MAX(m.monitoring) FROM MonitoringLog m GROUP BY m.device.id) ORDER BY ml.monitoring DESC")
    List<MonitoringLog> findLatestPingStatusForAllDevices();

    @Query("""
        SELECT ml
        FROM MonitoringLog ml
        WHERE ml.id IN (
            SELECT m1.id
            FROM MonitoringLog m1
            WHERE m1.monitoring = (
                SELECT MAX(m2.monitoring)
                FROM MonitoringLog m2
                WHERE m2.device.id = m1.device.id
            )
        )
    """)
    List<MonitoringLog> findLatestLogForEachDevices();


    
    @Query("SELECT ml FROM MonitoringLog ml WHERE ml.id IN (SELECT MAX(m.id) FROM MonitoringLog m GROUP BY m.device.id)")
    List<MonitoringLog> findLatestLogForEachDevice();

    // MonitoringLog findTopByDeviceOrderByMonitoringDesc(Device device);

    Optional<MonitoringLog> findTopByDeviceOrderByMonitoringDesc(Device device);

    List<MonitoringLog> findByDevice(Device device, Pageable pageable);

    Page<MonitoringLog> findByDeviceIn(List<Device> devices, Pageable pageable);

    // @Query(value = "SELECT m.* FROM monitoring_logs m " +
    //            "INNER JOIN (SELECT device_id, MAX(monitoring) AS max_monitoring " +
    //                        "FROM monitoring_logs GROUP BY device_id) latest " +
    //            "ON m.device_id = latest.device_id AND m.monitoring = latest.max_monitoring " +
    //            "ORDER BY m.monitoring DESC",
    //    nativeQuery = true)
    // List<MonitoringLog> findLatestLogPerDevice(Pageable pageable);

    @Query(value = "SELECT m.* FROM monitoring_logs m " +
               "INNER JOIN (SELECT device_id, MAX(monitoring) AS max_monitoring " +
                           "FROM monitoring_logs GROUP BY device_id) latest " +
               "ON m.device_id = latest.device_id AND m.monitoring = latest.max_monitoring " +
               "ORDER BY m.monitoring DESC",
       nativeQuery = true)
    Page<MonitoringLog> findLatestLogPerDevice(Pageable pageable);

    List<MonitoringLog> findAllByOrderByMonitoringDesc();


    @Modifying
    @Transactional
    @Query("UPDATE MonitoringLog m SET m.logReason = :reason WHERE m.logReason IS NULL")
    void setDefaultLogReason(@Param("reason") LogReason reason);

    @Query("SELECT ml FROM MonitoringLog ml WHERE ml.id IN " +
       "(SELECT MAX(m.id) FROM MonitoringLog m GROUP BY m.device.id) " +
       "ORDER BY ml.device.deviceName ASC")
    Page<MonitoringLog> findLatestLogPerDeviceOrderedByName(Pageable pageable);
}
