package com.unsia.netinv.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unsia.netinv.entity.BackupRoutes;
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;
import com.unsia.netinv.repository.BackupRouteRepository;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.FailOverLogRepository;

@Service
@Transactional
public class FailoverServiceImpl implements FailoverService {
    private static final Logger logger = LoggerFactory.getLogger(FailoverServiceImpl.class);

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

            Device backupDevice = deviceRepository.findById(backupRoute.getBackupDevice().getId())
                .orElseThrow(() -> new RuntimeException("Perangkat cadangan tidak ditemukan"));

            // 4. Aktifkan backup
            backupDevice.setStatusDevice("ONLINE");
            deviceRepository.save(backupDevice);

            // 5. Update status backup route
            backupRoute.setIsActive(true);
            backupRouteRepository.save(backupRoute);

            // 6. Buat log failover
            createFailoverLog(mainDevice, backupDevice, "SUCCESS");

            logger.info("Failover diaktifkan: {} -> {}", 
                mainDevice.getDeviceName(), backupDevice.getDeviceName());

        } catch (Exception e) {
            logger.error("Gagal mengaktifkan rute cadangan: {}", e.getMessage());
            throw new RuntimeException("Gagal mengaktifkan rute cadangan: " + e.getMessage());
        }
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
