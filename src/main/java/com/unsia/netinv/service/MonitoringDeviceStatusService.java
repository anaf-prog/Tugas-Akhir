package com.unsia.netinv.service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.netinve.LogReason;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;

@Service
public class MonitoringDeviceStatusService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @Autowired
    private PingService pingService;

    @Autowired
    private PingUpdateService pingUpdateService;

    @Autowired
    private MonitoringNotificationService monitoringNotificationService;

    @Autowired
    private FailoverService failoverService;

    // Memonitor status single device dengan ping
    public void monitorSingleDevice(Device device, Map<Long, Boolean> lastKnownStatus) {
        try {
            // Skip monitoring jika device dalam maintenance
            if ("MAINTENANCE".equals(device.getStatusDevice())) {
                System.out.println("Skipping monitoring for device in maintenance: " + device.getDeviceName());
                return;
            }

            boolean isOnline = pingService.pingDevice(device.getIpAddress());
            // System.out.println("Ping perangkat " + device.getDeviceName() + ": " + (isOnline ? "ONLINE" : "OFFLINE"));

            Long responseTime = pingService.pingWithResponseTime(device.getIpAddress());

            Date currentTime = new Date(); // Waktu sekarang untuk update

            // KIRIM UPDATE WEBSOCKET DI SINI SEBELUM HANDLE STATUS
            // Tentukan status untuk WebSocket
            String statusForWebSocket = determineWebSocketStatus(device, isOnline);
            pingUpdateService.sendPingUpdate(device.getId(), currentTime, statusForWebSocket);

            handleDeviceStatus(device, isOnline, responseTime, lastKnownStatus);
            
        } catch (Exception e) {
            System.out.println("Error saat monitoring perangkat : " + device.getDeviceName() + " : " + e.getMessage());
        }
    }

    private String determineWebSocketStatus(Device device, boolean isOnline) {
        if (!isOnline) {
            return "OFFLINE";
        }
        
        // Periksa apakah perangkat backup dan status aktif/tidak
        if (device.getDeviceName().toLowerCase().contains("backup")) {
            boolean isBackupActive = failoverService.isBackupActive(device.getId());
            return isBackupActive ? "ONLINE" : "ONLINE STANDBY";
        }
        return "ONLINE";
    }

    // Menangani perubahan status device
    private void handleDeviceStatus(Device device, boolean isOnline, Long responseTime, Map<Long, Boolean> lastKnownStatus) {
        // Jangan update status jika device dalam maintenance
        if ("MAINTENANCE".equals(device.getStatusDevice())) {
            return;
        }

        String newStatus = isOnline ? "ONLINE" : "OFFLINE";
        boolean statusChanged = !newStatus.equals(device.getStatusDevice());
        
        if (statusChanged) {
            updateDeviceStatus(device, newStatus, isOnline, responseTime, lastKnownStatus);
        } else if (isResponseTimeAnomaly(responseTime)) {
            createNewLog(device, isOnline, responseTime, LogReason.HIGH_LATENCY);
        } else {
            updateLastLog(device, isOnline, responseTime);
        }
    }

    // Update status device di database
    private void updateDeviceStatus(Device device, String newStatus, boolean isOnline, Long responseTime, Map<Long, Boolean> lastKnownStatus) {
        // Jangan update status jika device dalam maintenance
        if ("MAINTENANCE".equals(device.getStatusDevice())) {
            return;
        }

        // Simpan status sebelumnya
        String previousStatus = device.getStatusDevice();
        
        device.setStatusDevice(newStatus);
        deviceRepository.saveAndFlush(device); // Force immediate commit
        
        LogReason reason = isOnline ? LogReason.RECOVERED : LogReason.DOWN;
        createNewLog(device, isOnline, responseTime, reason);
        
        if (isOnline) {
            monitoringNotificationService.sendDeviceRecoveredNotification(device);
        } else {
            // Skip jika sebelumnya MAINTENANCE
            if (!"MAINTENANCE".equals(previousStatus)) {
                monitoringNotificationService.triggerDownNotifications(device);
            }
        }
        
        // Update last known status
        lastKnownStatus.put(device.getId(), isOnline);
    }

    private boolean isResponseTimeAnomaly(Long responseTime) {
        // Anggap response time > 500ms sebagai anomali
        return responseTime != null && responseTime > 500;
    }

    private void createNewLog(Device device, boolean isOnline, Long responseTime, LogReason reason) {
        try {
            Date now = new Date();
            
            MonitoringLog log = new MonitoringLog();
            log.setDevice(device);
            log.setPingStatus(isOnline);
            log.setResponseTime(responseTime != null ? responseTime.intValue() : null);
            log.setMonitoring(now);
            log.setLogReason(reason);
            
            monitoringLogRepository.save(log);
            
        } catch (Exception e) {
            System.err.println("ERROR CREATE LOG: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Update log terakhir jika ada
    private void updateLastLog(Device device, boolean isOnline, Long responseTime) {
        Optional<MonitoringLog> lastLogOpt = monitoringLogRepository.findTopByDeviceOrderByMonitoringDesc(device);
        
        if (lastLogOpt.isPresent()) {
            MonitoringLog lastLog = lastLogOpt.get();
            // Update log yang ada jika reason-nya Normal
            if (lastLog.getLogReason() == LogReason.NORMAL || lastLog.getPingStatus() == isOnline) {
                lastLog.setPingStatus(isOnline);
                lastLog.setResponseTime(responseTime != null ? responseTime.intValue() : null);
                lastLog.setMonitoring(new Date());
                monitoringLogRepository.save(lastLog);
            } else {
                // Buat log baru jika log terakhir bukan Normal
                LogReason reason = isOnline ? LogReason.RECOVERED : LogReason.DOWN;
                createNewLog(device, isOnline, responseTime, reason);
            }
        } else {
            // Buat log baru jika tidak ada log sama sekali
            createNewLog(device, isOnline, responseTime, LogReason.NORMAL);
        }
    }
    
}
