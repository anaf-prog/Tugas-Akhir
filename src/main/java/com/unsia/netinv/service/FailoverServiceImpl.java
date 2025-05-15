package com.unsia.netinv.service;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.BackupRoutes;
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;
import com.unsia.netinv.repository.BackupRouteRepository;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.FailOverLogRepository;

@Service
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
            Optional<BackupRoutes> backupRouteOpt = backupRouteRepository.findByMainDeviceId(mainDeviceId);

            if (!backupRouteOpt.isPresent()) {
                logger.info("Tidak ada rute cadangan untuk perangkat ID {}", mainDeviceId);
                return;
            }

            BackupRoutes backupRoute = backupRouteOpt.get();

            Device mainDevice = deviceRepository.findById(mainDeviceId)
                .orElseThrow(() -> new RuntimeException("Perangkat utama tidak ditemukan!"));

            Device backupDevice = backupRoute.getBackupDevice();
            if (backupDevice == null) {
                throw new RuntimeException("Perangkat cadangan tidak ditemukan!");
            }

            // Pastikan perangkat utama benar-benar offline
            if (!"OFFLINE".equals(mainDevice.getStatusDevice())) {
                logger.info("Perangkat utama {} tidak offline, skip failover", mainDevice.getDeviceName());
                return;
            }

            // Aktifkan backup
            backupDevice.setStatusDevice("ONLINE");
            deviceRepository.save(backupDevice);

            // Update status backup route
            backupRoute.setIsActive(true);
            backupRouteRepository.save(backupRoute);

            // Buat log
            FailOverLogs log = new FailOverLogs();
            log.setWaktu(new Date());
            log.setMainDevice(mainDevice);
            log.setBackupDevice(backupDevice);
            log.setStatus("SUCCESS");
            log.setResponseTimeMs(calculateResponseTime(backupDevice));
            failOverLogRepository.save(log);

            logger.info("Failover diaktifkan: {} -> {}", 
                mainDevice.getDeviceName(), backupDevice.getDeviceName());

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Gagal mengaktifkan rute cadangan: {}", e.getMessage());
            throw new RuntimeException("Gagal mengaktifkan rute cadangan: " + e.getMessage());
        }
    }

    // Method untuk menghitung response time (contoh sederhana)
    private Integer calculateResponseTime(Device device) {
        return (int) (Math.random() * 150) + 50;
    }
    
}
