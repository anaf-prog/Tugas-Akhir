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
    private FailOverLogRepository failOverLogRepository;

    @Autowired
    private MonitoringDeviceStatusService monitoringDeviceStatusService;

    @Autowired
    private MonitoringNotificationService monitoringNotificationService;

    private final Map<Long, Boolean> lastKnownStatus = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize last known status saat aplikasi mulai
        List<Device> devices = deviceRepository.findAll();
        devices.forEach(device -> 
            lastKnownStatus.put(device.getId(), "ONLINE".equals(device.getStatusDevice())));
    }

    // ini masih di test setiap detik ping nya
    @Override
    @Scheduled(fixedRate = 1200)
    public void monitoringAlldevices() {
        List<Device> devices = deviceRepository.findAll();
        // System.out.println("\n=== Starting monitoring for " + devices.size() + " devices at " + new Date() + " ===");

        // Cek ada perubahan manual gak
        checkForManualStatusChanges();

        // Lanjut normal monitoring
        for (Device device : devices) {
            monitoringDeviceStatusService.monitorSingleDevice(device, lastKnownStatus);
        }

        // System.out.println("=== Monitoring cycle completed at " + new Date() + " ===\n");
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
                monitoringNotificationService.handleManualStatusChange(device, currentStatus);
            }
            
            // Update last known status
            lastKnownStatus.put(device.getId(), currentStatus);
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
