package com.unsia.netinv.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MonitoringLog;

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
    
}
