package com.unsia.netinv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.netinve.LogReason;
import com.unsia.netinv.repository.DeviceRepository;

@Service
public class MonitoringNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringNotificationService.class);

    private static final String ANSI_YELLOW = "\u001B[33m";
    @SuppressWarnings("unused")
    private static final String ANSI_RED = "\u001B[31m";

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private DashboardNotificationService dashboardNotificationService;

    @Autowired
    private FailoverService failoverService;

    // Menangani perubahan status manual
    public void handleManualStatusChange(Device device, boolean newStatus) {
        String currentStatus = device.getStatusDevice();

        // Periksa apakah device dalam status maintenance
        if ("MAINTENANCE".equals(currentStatus) && newStatus) {
            device.setStatusDevice("ONLINE");
            deviceRepository.saveAndFlush(device);

            // Maintenance selesai
            sendMaintenanceEndNotification(device);
            return;
        }

        // Skip jika dalam maintenance dan akan dimatikan
        if ("MAINTENANCE".equals(currentStatus) && !newStatus) {
            return;
        }

        @SuppressWarnings("unused")
        LogReason reason = newStatus ? LogReason.DOWN : LogReason.RECOVERED;
        // createNewLog dipindah ke MonitoringDeviceStatusService
        
        if (!newStatus) {
            // Skip failover jika dalam maintenance
            if (!"MAINTENANCE".equals(device.getStatusDevice())) {
                triggerDownNotifications(device);
            }
        } else {
            sendDeviceRecoveredNotification(device);
        }
    }

    // Trigger notifikasi ketika device down
    public void triggerDownNotifications(Device device) {
        // Dapatkan status terbaru dari database
        Device refreshedDevice = deviceRepository.findById(device.getId()).orElse(device);

        // Periksa apakah ini maintenance
        if ("MAINTENANCE".equals(refreshedDevice.getStatusDevice())) {
            sendMaintenanceNotification(device);
            return;
        }

        // Lanjutkan dengan failover hanya jika bukan maintenance
        try {
            failoverService.activateBackupRoute(device.getId());
            logger.info(ANSI_YELLOW + "Berhasil aktifkan perangkat back up : " + device.getDeviceName() + " dengan ID : " + device.getId());
            
            sendDeviceDownNotification(device, "Sistem telah melakukan failover otomatis ke perangkat cadangan");
        } catch (Exception e) {
            System.out.println("Failover failed for " + device.getDeviceName() + ": " + e.getMessage());
            sendDeviceDownNotification(device, "Gagal melakukan failover otomatis");
        }
    }

    public void sendDeviceRecoveredNotification(Device device) {
        dashboardNotificationService.sendDeviceRecoveredNotification(device, "Perangkat sudah kembali normal");
    }

    public void sendMaintenanceEndNotification(Device device) {
        dashboardNotificationService.sendMaintenanceEndNotification(device, "Pemeliharaan perangkat telah selesai");
        emailNotificationService.sendMaintenanceEndNotification(device, "Pemeliharaan perangkat telah selesai");
    }

    public void sendMaintenanceNotification(Device device) {
        emailNotificationService.sendMaintenanceNotification(device, "Perangkat sedang dalam pemeliharaan");
        dashboardNotificationService.sendMaintenanceNotification(device, "Perangkat sedang dalam pemeliharaan");
    }

    public void sendDeviceDownNotification(Device device, String message) {
        emailNotificationService.sendDeviceDownNotification(device, message);
        dashboardNotificationService.sendDeviceDownNotification(device, message);
    }
    
}
