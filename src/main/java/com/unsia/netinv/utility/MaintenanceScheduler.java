package com.unsia.netinv.utility;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MaintenanceLog;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MaintenanceLogRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;

import jakarta.annotation.PostConstruct;

@Component
public class MaintenanceScheduler {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceScheduler.class);
    
    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        schedulePendingMaintenance();
    }

    public void schedulePendingMaintenance() {
        List<MaintenanceLog> pendingMaintenance = maintenanceLogRepository
            .findByScheduledTimeAfterAndAutoDisable(LocalDateTime.now(), true);
        
        pendingMaintenance.forEach(log -> {
            try {
                this.scheduleDeviceDisable(log);
            } catch (Exception e) {
                logger.error("Failed to schedule maintenance for device {}. Exception:", log.getDevice().getId(), e);

            }
        });
    }

    public void scheduleDeviceDisable(MaintenanceLog maintenanceLog) {
        try {
            LocalDateTime scheduledTime = maintenanceLog.getScheduledTime();
            long delay = ChronoUnit.MILLIS.between(LocalDateTime.now(), scheduledTime);
            
            logger.info("Scheduling device disable for {} at {} (in {} ms)", 
                maintenanceLog.getDevice().getId(), scheduledTime, delay);
            
            if (delay > 0) {
                scheduler.schedule(() -> {
                    try {
                        this.executeDeviceDisable(maintenanceLog);
                    } catch (Exception e) {
                        logger.error("Error executing device disable task", e);
                    }
                }, delay, TimeUnit.MILLISECONDS);
            } else {
                // Jika waktu sudah lewat, langsung eksekusi
                this.executeDeviceDisable(maintenanceLog);
            }
        } catch (Exception e) {
            logger.error("Error scheduling device disable", e);
        }
    }

    private void executeDeviceDisable(MaintenanceLog maintenanceLog) {
        try {
            logger.info("Executing auto-disable for device {}", maintenanceLog.getDevice().getId());
            
            // Dapatkan device terbaru dari database
            Optional<Device> deviceOpt = deviceRepository.findById(maintenanceLog.getDevice().getId());
            if (!deviceOpt.isPresent()) {
                logger.warn("Device not found for maintenance log {}", maintenanceLog.getId());
                return;
            }

            Device device = deviceOpt.get();
            logger.info("Current device status before disable: {}", device.getStatusDevice());
            
            // Update status device
            device.setStatusDevice("MAINTENANCE");
            device.setLastChecked(new Date());
            deviceRepository.save(device);
            
            logger.info("Device status after disable: {}", device.getStatusDevice());
            
            // Buat monitoring log
            MonitoringLog monitoringLog = new MonitoringLog();
            monitoringLog.setDevice(device);
            monitoringLog.setPingStatus(false);
            monitoringLog.setMonitoring(new Date());
            monitoringLogRepository.save(monitoringLog);
            
            logger.info("Successfully disabled device {} by maintenance schedule", device.getId());
        } catch (Exception e) {
            logger.error("Failed to execute device disable", e);
        }
    }
}
