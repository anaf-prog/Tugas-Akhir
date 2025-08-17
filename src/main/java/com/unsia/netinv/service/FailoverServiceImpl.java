package com.unsia.netinv.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unsia.netinv.entity.BackupRoutes;
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.repository.BackupRouteRepository;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.FailOverLogRepository;

@Service
@Transactional
public class FailoverServiceImpl implements FailoverService {
    private static final Logger logger = LoggerFactory.getLogger(FailoverServiceImpl.class);

    @SuppressWarnings("unused")
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";


    @Autowired
    BackupRouteRepository backupRouteRepository;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    FailOverLogRepository failOverLogRepository;

    @Override
    public void activateBackupRoute(Long mainDeviceId) {

        try {
            Device mainDevice = deviceRepository.findByIdWithLock(mainDeviceId)
                .orElseThrow(() -> new RuntimeException("Perangkat utama tidak ditemukan!"));  

            // 1. Cek apakah ini maintenance, jika ya, tidak perlu failover
            if ("MAINTENANCE".equals(mainDevice.getStatusDevice())) {
                logger.info("Perangkat {} dalam status MAINTENANCE, skip failover", 
                    mainDevice.getDeviceName());
                return;
            }

            // 2. Cek apakah perangkat utama benar-benar offline
            if (!"OFFLINE".equals(mainDevice.getStatusDevice())) {
                logger.info("Perangkat utama {} tidak offline, skip failover", 
                    mainDevice.getDeviceName());
                return;
            }

            // 3. Cari rute cadangan
            BackupRoutes backupRoute = backupRouteRepository.findByMainDeviceId(mainDeviceId)
                .orElseThrow(() -> new RuntimeException("Tidak ada rute cadangan untuk perangkat ID: " + mainDeviceId));

            Device backupDevice = backupRoute.getBackupDevice();

            // Tambahkan pengecekan apakah perangkat utama sudah online kembali
            checkAndRepairExistingFailovers(mainDeviceId);

            // 4. Buat log failover
            createFailoverLog(mainDevice, backupDevice, "SUCCESS");

            logger.warn(ANSI_RED + "Failover diaktifkan : {} -> {}", mainDevice.getDeviceName(), backupDevice.getDeviceName());

        } catch (Exception e) {
            logger.error("Gagal mengaktifkan rute cadangan: {}", e.getMessage());
            throw new RuntimeException("Gagal mengaktifkan rute cadangan: " + e.getMessage());
        }
    }

    @Override
    public boolean isBackupActive(Long backupDeviceId) {
        // Cari semua main device yang menggunakan backup ini
        List<BackupRoutes> backupRoutes = backupRouteRepository.findAllByBackupDeviceId(backupDeviceId);
        
        // Cek apakah ada minimal satu main device yang offline dan memiliki failover aktif
        return backupRoutes.stream().anyMatch(route -> {
            Device mainDevice = route.getMainDevice();
            boolean isMainOffline = "OFFLINE".equals(mainDevice.getStatusDevice());
            boolean hasActiveFailover = failOverLogRepository.existsActiveFailover(mainDevice, route.getBackupDevice());
            return isMainOffline && hasActiveFailover;
        });
    }

    @Override
    public void repairFailover(Long mainDeviceId) {
        Device mainDevice = deviceRepository.findById(mainDeviceId)
            .orElseThrow(() -> new RuntimeException("Perangkat tidak ditemukan"));

        Optional<FailOverLogs> activeFailover = failOverLogRepository.findTopByMainDeviceAndRepairTimeIsNullOrderByWaktuDesc(mainDevice);
        
        if (activeFailover.isPresent()) {
            FailOverLogs log = activeFailover.get();
            log.setRepairTime(LocalDateTime.now());
            failOverLogRepository.save(log);
            logger.info("Failover diperbaiki untuk perangkat utama: {}", log.getMainDevice().getDeviceName());
        }
    }

    // Method baru untuk mengecek dan memperbaiki failover yang sudah tidak diperlukan
    private void checkAndRepairExistingFailovers(Long mainDeviceId) {    
        List<FailOverLogs> activeFailovers = failOverLogRepository.findByMainDeviceIdAndRepairTimeIsNull(mainDeviceId);
        
        Device mainDevice = deviceRepository.findById(mainDeviceId)
            .orElseThrow(() -> new RuntimeException("Perangkat tidak ditemukan"));

        if ("ONLINE".equals(mainDevice.getStatusDevice()) && !activeFailovers.isEmpty()) {
            activeFailovers.forEach(failover -> {
                failover.setRepairTime(LocalDateTime.now());
                failOverLogRepository.save(failover);
                logger.info("Auto-repair failover untuk perangkat utama yang kembali online: {}", 
                    mainDevice.getDeviceName());
            });
        }
    }

    @Override
    public Map<String, String> getDeviceStatusInfo(MonitoringLog log) {
        Map<String, String> statusInfo = new HashMap<>();
        statusInfo.put("pingClass", "ping-inactive");
        statusInfo.put("badgeClass", "bg-danger");

        // Pastikan log dan device valid
        if (log == null || log.getDevice() == null || log.getPingStatus() == null) {
            return statusInfo;
        }

        try {
            boolean isBackup = log.getDevice().getDeviceName().toLowerCase().contains("backup");
            boolean isOnline = log.getPingStatus();
            
            if (isOnline) {
                if (isBackup) {
                    boolean isActive = this.isBackupActive(log.getDevice().getId());
                    statusInfo.put("pingClass", isActive ? "ping-active" : "ping-backup");
                    statusInfo.put("badgeClass", isActive ? "bg-success" : "bg-warning");
                } else {
                    statusInfo.put("pingClass", "ping-active");
                    statusInfo.put("badgeClass", "bg-success");
                }
            }
        } catch (Exception e) {
            logger.error("Error getting device status info: {}", e.getMessage());
        }
        
        return statusInfo;
    }

    private void createFailoverLog(Device mainDevice, Device backupDevice, String status) {
        FailOverLogs log = new FailOverLogs();
        log.setWaktu(new Date());
        log.setMainDevice(mainDevice);
        log.setBackupDevice(backupDevice);
        log.setStatus(status);
        log.setResponseTimeMs(calculateResponseTime(backupDevice));
        failOverLogRepository.save(log);
    }

    // Method untuk menghitung response time (contoh sederhana)
    private Integer calculateResponseTime(Device device) {
        return (int) (Math.random() * 101) + 950;
    }
}
