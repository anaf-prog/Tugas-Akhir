package com.unsia.netinv.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.netinve.LogReason;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;

import jakarta.annotation.PostConstruct;

@Service
public class SimulationServiceImpl implements SimulationService {
    private final Map<String, LocalDateTime> failureSchedule = new ConcurrentHashMap<>();
    private final Set<String> currentlyFailedDevice = ConcurrentHashMap.newKeySet();

    @Autowired
    private PingService pingService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @PostConstruct
    @Override
    public void init() {
        // scheduleFailure("192.168.1.6", 1);
        // scheduleFailure("192.168.1.9", 1);
    }

    @Override
    public void scheduleFailure(String ipAddress, int minutesToFailure) {
        LocalDateTime failureTime = LocalDateTime.now().plusMinutes(minutesToFailure);
        failureSchedule.put(ipAddress, failureTime);
        System.out.println("Failure scheduled for " + ipAddress + " at " + failureTime);
    }

    @Override
    public void cancelScheduleFailure(String ipAddress) {
        failureSchedule.remove(ipAddress);
        System.out.println("Cancelled scheduled failure for : " + ipAddress);
    }

    @Override
    public void recoveryDevice(String ipAddress) {

        Device device = deviceRepository.findByIpAddress(ipAddress);
        if (device == null) {
            throw new IllegalArgumentException("Device dengan IP " + ipAddress + " tidak ditemukan");
        }

        // Update status device
        device.setStatusDevice("ONLINE");
        device.setPingStatus("ONLINE");
        deviceRepository.save(device);

        // Buat log recovery
        MonitoringLog log = new MonitoringLog();
        log.setDevice(device);
        log.setPingStatus(true);
        log.setMonitoring(new Date());
        log.setLogReason(LogReason.RECOVERED);
        monitoringLogRepository.save(log);

        // Jika device ada di daftar failed, hapus
        if (currentlyFailedDevice.contains(ipAddress)) {
            pingService.forceFailure(ipAddress, false);
            currentlyFailedDevice.remove(ipAddress);
        }
        
        System.out.println("Device recovered: " + ipAddress);
    }

    @Override
    public void recoveryAllDevices() {
        for (String ipAddress : currentlyFailedDevice) {
            pingService.forceFailure(ipAddress, false);
        }
        currentlyFailedDevice.clear();
        System.out.println("All device recovered");
    }

    @Scheduled(fixedRate = 5000)
    @Override
    public void checkScheduleFailures() {
        LocalDateTime now = LocalDateTime.now();

        for (Map.Entry<String, LocalDateTime> entry : failureSchedule.entrySet()) {
            String ipAddress = entry.getKey();
            LocalDateTime failureTime = entry.getValue();

            if (now.isAfter(failureTime)) {
                pingService.forceFailure(ipAddress, true);
                currentlyFailedDevice.add(ipAddress);
                failureSchedule.remove(ipAddress);
                System.out.println("Simulasi failure aktif : " + ipAddress);
            }
        }
    }

    @Override
    public Set<String> getFaileddevices() {
        return Collections.unmodifiableSet(currentlyFailedDevice);
    }
    
}
