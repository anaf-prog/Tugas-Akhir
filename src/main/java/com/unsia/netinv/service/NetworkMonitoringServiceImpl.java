package com.unsia.netinv.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.netinve.LogReason;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.FailOverLogRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;

import jakarta.annotation.PostConstruct;

@Service
public class NetworkMonitoringServiceImpl implements NetworkMonitoringService {
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(NetworkMonitoringServiceImpl.class);

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private DashboardNotificationService dashboardNotificationService;

    @Autowired
    private PingService pingService;

    @Autowired
    private FailoverService failoverService;

    @Autowired
    private FailOverLogRepository failOverLogRepository;

    private final Map<Long, Boolean> lastKnownStatus = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize last known status saat aplikasi mulai
        List<Device> devices = deviceRepository.findAll();
        devices.forEach(device -> 
            lastKnownStatus.put(device.getId(), "ONLINE".equals(device.getStatusDevice())));
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void monitoringAlldevices() {
        List<Device> devices = deviceRepository.findAll();
        System.out.println("Starting monitoring for " + devices.size() + " devices...");

        // Check for manual changes first
        checkForManualStatusChanges();

        // Continue normal monitoring
        for (Device device : devices) {
            monitorSingleDevice(device);
        }
    }

    // Memeriksa perubahan status manual di database
    private void checkForManualStatusChanges() {
        List<Device> devices = deviceRepository.findAll();
        for (Device device : devices) {
            // Skip jika device dalam maintenance
            if ("MAINTENANCE".equals(device.getStatusDevice())) {
                continue;
            }
            
            Boolean lastStatus = lastKnownStatus.get(device.getId());
            boolean currentStatus = "ONLINE".equals(device.getStatusDevice());
            
            if (lastStatus != null && lastStatus != currentStatus) {
                System.out.println("Manual status change detected for device: " + device.getDeviceName());
                handleManualStatusChange(device, currentStatus);
            }
            
            // Update last known status
            lastKnownStatus.put(device.getId(), currentStatus);
        }
    }

    // Menangani perubahan status manual
    private void handleManualStatusChange(Device device, boolean newStatus) {
        String currentStatus = device.getStatusDevice();

        // Periksa apakah device dalam status maintenance
        if ("MAINTENANCE".equals(currentStatus) && newStatus) {
            device.setStatusDevice("ONLINE");
            deviceRepository.saveAndFlush(device);

            // Maintenance selesai
            createNewLog(device, true, null, LogReason.MAINTENANCE_END);
            dashboardNotificationService.sendMaintenanceEndNotification(
                device, "Pemeliharaan perangkat telah selesai");
            emailNotificationService.sendMaintenanceEndNotification(
                device, "Pemeliharaan perangkat telah selesai");
            return;
        }

        // Skip jika dalam maintenance dan akan dimatikan
        if ("MAINTENANCE".equals(currentStatus) && !newStatus) {
            return;
        }

        LogReason reason = newStatus ? LogReason.DOWN : LogReason.RECOVERED;
        createNewLog(device, newStatus, null, reason);
        
        if (!newStatus) {
            // Skip failover jika dalam maintenance
            if (!"MAINTENANCE".equals(device.getStatusDevice())) {
                triggerDownNotifications(device);
            }
        } else {
            dashboardNotificationService.sendDeviceRecoveredNotification(
                device, "Perangkat sudah kembali normal");
        }
    }

    // Memonitor status single device dengan ping
    private void monitorSingleDevice(Device device) {
        try {
            // Skip monitoring jika device dalam maintenance
            if ("MAINTENANCE".equals(device.getStatusDevice())) {
                System.out.println("Skipping monitoring for device in maintenance: " + device.getDeviceName());
                return;
            }

            boolean isOnline = pingService.pingDevice(device.getIpAddress());
            Long responseTime = pingService.pingWithResponseTime(device.getIpAddress());
            
            // System.out.println("Monitoring " + device.getIpAddress() + 
            //     " - DB Status: " + device.getStatusDevice() + 
            //     ", Ping: " + isOnline);

            handleDeviceStatus(device, isOnline, responseTime);
            
        } catch (Exception e) {
            System.out.println("Error monitoring device " + device.getDeviceName() + 
                ": " + e.getMessage());
        }
    }

    // Menangani perubahan status device
    private void handleDeviceStatus(Device device, boolean isOnline, Long responseTime) {
        // Jangan update status jika device dalam maintenance
        if ("MAINTENANCE".equals(device.getStatusDevice())) {
            return;
        }

        String newStatus = isOnline ? "ONLINE" : "OFFLINE";
        boolean statusChanged = !newStatus.equals(device.getStatusDevice());
        
        if (statusChanged) {
            updateDeviceStatus(device, newStatus, isOnline, responseTime);
        } else if (isResponseTimeAnomaly(responseTime)) {
            createNewLog(device, isOnline, responseTime, LogReason.HIGH_LATENCY);
        } else {
            updateLastLog(device, isOnline, responseTime);
        }
    }

    // Update status device di database
    private void updateDeviceStatus(Device device, String newStatus, boolean isOnline, Long responseTime) {
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
            dashboardNotificationService.sendDeviceRecoveredNotification(
                device, "Perangkat kembali online");
        } else {
            // Skip jika sebelumnya MAINTENANCE
            if (!"MAINTENANCE".equals(previousStatus)) {
                triggerDownNotifications(device);
            }
        }
        
        // Update last known status
        lastKnownStatus.put(device.getId(), isOnline);
    }

    // Trigger notifikasi ketika device down
    private void triggerDownNotifications(Device device) {
        // Dapatkan status terbaru dari database
        Device refreshedDevice = deviceRepository.findById(device.getId()).orElse(device);

        // Periksa apakah ini maintenance
        if ("MAINTENANCE".equals(refreshedDevice.getStatusDevice())) {
            emailNotificationService.sendMaintenanceNotification(
                device, "Perangkat sedang dalam pemeliharaan");
            dashboardNotificationService.sendMaintenanceNotification(
                device, "Perangkat sedang dalam pemeliharaan");
            return;
        }

         // Lanjutkan dengan failover hanya jika bukan maintenance
        try {
            failoverService.activateBackupRoute(device.getId());
            System.out.println("Berhasil aktifkan perangkat back up " + device.getDeviceName() + " dengan ID : " + device.getId());
            
            emailNotificationService.sendDeviceDownNotification(device,
                "Sistem telah melakukan failover otomatis ke perangkat cadangan");
            dashboardNotificationService.sendDeviceDownNotification(device,
                "Sistem telah melakukan failover otomatis ke perangkat cadangan");
        } catch (Exception e) {
            System.out.println("Failover failed for " + device.getDeviceName() + ": " + e.getMessage());
            
            emailNotificationService.sendDeviceDownNotification(device,
                "Gagal melakukan failover otomatis");
            dashboardNotificationService.sendDeviceDownNotification(device,
                "Gagal melakukan failover otomatis");
        }
    }

    private boolean isResponseTimeAnomaly(Long responseTime) {
        // Anggap response time > 500ms sebagai anomali
        return responseTime != null && responseTime > 500;
    }

    private void createNewLog(Device device, boolean isOnline, Long responseTime, LogReason reason) {
        MonitoringLog log = new MonitoringLog();
        log.setDevice(device);
        log.setPingStatus(isOnline);
        log.setResponseTime(responseTime != null ? responseTime.intValue() : null);
        log.setMonitoring(new Date());
        log.setLogReason(reason);
        monitoringLogRepository.save(log);

        if (isOnline && reason == LogReason.RECOVERED) {
            updateFailoverRepairTime(device, log.getMonitoring());
        }

        // logger.debug("Created NEW log for device {} with reason {}", device.getDeviceName(), reason);
        System.out.println("Created NEW log for device {} with reason {}" + device.getDeviceName() + reason);
    }

    // Update log terakhir jika ada
    private void updateLastLog(Device device, boolean isOnline, Long responseTime) {
        @SuppressWarnings("unused")
        Pageable latest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "monitoring"));
        Optional<MonitoringLog> lastLogOpt = monitoringLogRepository.findTopByDeviceOrderByMonitoringDesc(device);

        // System.out.println("Latest : " + latest);
        
        if (lastLogOpt.isPresent()) {
            MonitoringLog lastLog = lastLogOpt.get();
            // Update log yang ada jika reason-nya Normal
            if (lastLog.getLogReason() == LogReason.NORMAL || lastLog.getPingStatus() == isOnline) {
                lastLog.setPingStatus(isOnline);
                lastLog.setResponseTime(responseTime != null ? responseTime.intValue() : null);
                lastLog.setMonitoring(new Date());
                monitoringLogRepository.save(lastLog);
                // logger.debug("UPDATED existing log for device {}", device.getDeviceName());
                // System.out.println("UPDATED existing log for device {}" + device.getDeviceName());
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

    @Override
    public List<Device> checkFailedDevices() {
        return deviceRepository.findByStatusDevice("OFFLINE");
    }

    // Mendapatkan log monitoring terbaru
    @Override
    public List<MonitoringLog> getLatestMonitoringLogs(int count) {
        List<Device> devices = deviceRepository.findAll();
        List<MonitoringLog> latestLogs = new ArrayList<>();

        for (Device device : devices) {
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "monitoring"));
            List<MonitoringLog> logs = monitoringLogRepository.findByDevice(device, pageable);
            if (!logs.isEmpty()) {
                latestLogs.add(logs.get(0));
            }
        }

        latestLogs.sort((a, b) -> b.getMonitoring().compareTo(a.getMonitoring()));
        return latestLogs.stream().limit(count).collect(Collectors.toList());
    }

    // Update waktu perbaikan failover
    @Override
    public void updateFailoverRepairTime(Device device, Date repairDate) {
        // Cari failover terakhir yang belum diperbaiki
        Optional<FailOverLogs> openFailover = 
            failOverLogRepository.findTopByMainDeviceAndRepairTimeIsNullOrderByWaktuDesc(device);

        // Jika ada, set repairTime = waktu monitoring log (waktu ONLINE)
        openFailover.ifPresent(failover -> {
            failover.setRepairTime(
                repairDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            );
            failOverLogRepository.save(failover);
        });
    }
}
