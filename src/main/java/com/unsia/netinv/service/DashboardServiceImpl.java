package com.unsia.netinv.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.FailOverLogRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;
import com.unsia.netinv.repository.ReportRepository;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @Autowired
    private FailOverLogRepository failOverLogRepository;

    @Autowired
    ReportRepository reportRepository;

    @Override
    public Map<String, Object> getDashboardData(int devicePage, int deviceSize, int logPage, int logSize,
                                                int failoverPage, int failoverSize, 
                                                String search, String status, String type, Users user) {
        Map<String, Object> data = new HashMap<>();

        List<Device> offlinDevices = deviceRepository.findByStatusDevice("OFFLINE");

        for (Device device : offlinDevices) {
            Optional<MonitoringLog> latestLogOpt = monitoringLogRepository.findTopByDeviceOrderByMonitoringDesc(device);

            if (latestLogOpt.isPresent()) {
                MonitoringLog log = latestLogOpt.get();
                // Jika status ping masih true (ONLINE), update ke false (OFFLINE)
                if (Boolean.TRUE.equals(log.getPingStatus())) {
                    log.setPingStatus(false);
                    monitoringLogRepository.save(log);
                }
            } else {
                // Jika tidak ada log, buat log baru dengan status OFFLINE
                MonitoringLog newLog = new MonitoringLog();
                newLog.setDevice(device);
                newLog.setPingStatus(false);
                newLog.setMonitoring(new Date());
                monitoringLogRepository.save(newLog);
            }
        }

        Specification<Device> countSpec = new DeviceSpecification(search, status, type);

        long totalDevices = deviceRepository.count(countSpec);
        long activeDevices = deviceRepository.count(countSpec.and((root, query, cb) -> cb.equal(root.get("statusDevice"), "ONLINE")));
        long downDevices = deviceRepository.count(countSpec.and((root, query, cb) -> cb.equal(root.get("statusDevice"), "OFFLINE")));

        Specification<Device> routerSpec = countSpec.and((root, query, cb) -> cb.equal(root.get("deviceType"), "ROUTER"));
        Specification<Device> switchSpec = countSpec.and((root, query, cb) -> cb.equal(root.get("deviceType"), "SWITCH"));
        Specification<Device> serverSpec = countSpec.and((root, query, cb) -> cb.equal(root.get("deviceType"), "SERVER"));

        long routerCount = deviceRepository.count(routerSpec);
        long switchCount = deviceRepository.count(switchSpec);
        long serverCount = deviceRepository.count(serverSpec);

        Pageable devicePageable = PageRequest.of(devicePage, deviceSize, Sort.by("deviceName").ascending());
        Page<Device> devicePageResult = deviceRepository.findAll(new DeviceSpecification(search, status, type), devicePageable);

        Pageable logPageable = PageRequest.of(logPage, logSize);
        Page<MonitoringLog> logPageResult = monitoringLogRepository.findLatestLogPerDeviceOrderedByName(logPageable);

        Pageable failoverPageable = PageRequest.of(failoverPage, failoverSize, Sort.by("waktu").descending());
        Page<FailOverLogs> failoverPageResult = failOverLogRepository.findAllByOrderByWaktuDesc(failoverPageable);

        List<FailOverLogs> failoverLogs = failoverPageResult.getContent();

        for (FailOverLogs log : failoverLogs) {
            if (log.getRepairTime() == null) {
                LocalDateTime waktuDown = log.getWaktu().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

                long seed = log.getMainDevice().getId();
                int minutesToAdd = 30 + new Random(seed).nextInt(90);
                LocalDateTime estimatedTime = waktuDown.plusMinutes(minutesToAdd);
                
                log.setEstimatedRepairTime(estimatedTime);
            }
        }

        // Urutkan secara manual
        List<MonitoringLog> sortedLogs = logPageResult.getContent().stream()
            .sorted(Comparator.comparing(log -> log.getDevice().getDeviceName()))
            .collect(Collectors.toList());
        
        // Buat Page baru dengan data yang sudah diurutkan
        Page<MonitoringLog> sortedLogPage = new PageImpl<>(
            sortedLogs, 
            logPageable, 
            logPageResult.getTotalElements()
        );


        data.put("currentUser", user.getUsername());
        data.put("userRole", user.getRole());
        data.put("totalDevices", totalDevices);
        data.put("activeDevices", activeDevices);
        data.put("downDevices", downDevices);
        data.put("routerCount", routerCount);
        data.put("switchCount", switchCount);
        data.put("serverCount", serverCount);

        data.put("devices", devicePageResult.getContent());
        data.put("deviceCurrentPage", devicePageResult.getNumber());
        data.put("deviceTotalPages", devicePageResult.getTotalPages());
        data.put("totalItems", devicePageResult.getTotalElements());

        data.put("latestLogs", sortedLogPage.getContent());
        data.put("logCurrentPage", logPageResult.getNumber());
        data.put("logTotalPages", logPageResult.getTotalPages());
        data.put("logTotalItems", logPageResult.getTotalElements());

        data.put("failoverLogs", failoverPageResult.getContent());
        data.put("failoverCurrentPage", failoverPageResult.getNumber());
        data.put("failoverTotalPages", failoverPageResult.getTotalPages());
        data.put("failoverTotalItems", failoverPageResult.getTotalElements());

        return data;
    }

    @Override
    public List<Map<String, Object>> getDownDevices() {
        List<Device> downDevices = deviceRepository.findByStatusDevice("OFFLINE");

        return downDevices.stream().map(device -> {
            Map<String, Object> deviceMap = new LinkedHashMap<>();
            deviceMap.put("device_name", device.getDeviceName());
            deviceMap.put("ip_address", device.getIpAddress());
            deviceMap.put("device_type", device.getDeviceType());
            deviceMap.put("status_device", device.getStatusDevice());
            deviceMap.put("last_checked", device.getLastChecked());

            if (device.getLocation() != null) {
                Map<String, Object> locationMap = new LinkedHashMap<>();
                locationMap.put("location_name", device.getLocation().getLocationName());
                locationMap.put("room", device.getLocation().getRoom());
                locationMap.put("floor", device.getLocation().getFloor());
                deviceMap.put("location", locationMap);
            }

            return deviceMap;
        }).collect(Collectors.toList());
    }
    
}
