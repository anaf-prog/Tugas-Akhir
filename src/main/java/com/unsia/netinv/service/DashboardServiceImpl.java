package com.unsia.netinv.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.FailOverLogs;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.entity.Report;
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
    private ReportRepository reportRepository;

    @Override
    public Map<String, Object> getDashboardData(int devicePage, int deviceSize, int logPage, int logSize,
                                                int failoverPage, int failoverSize, 
                                                String search, String status, String type, Users user) {
        Map<String, Object> data = new HashMap<>();

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

        Pageable logPageable = PageRequest.of(logPage, logSize, Sort.by("monitoring").descending());
        Page<MonitoringLog> logPageResult = monitoringLogRepository.findLatestLogPerDevice(logPageable);

        Pageable failoverPageable = PageRequest.of(failoverPage, failoverSize, Sort.by("waktu").descending());
        Page<FailOverLogs> failoverPageResult = failOverLogRepository.findAllByOrderByWaktuDesc(failoverPageable);

        List<FailOverLogs> failoverLogs = failoverPageResult.getContent();
        LocalDateTime now = LocalDateTime.now();

        for (FailOverLogs log : failoverLogs) {
            // Jika sudah ada repairTime yang disimpan, gunakan itu
            if (log.getRepairTime() != null) {
                continue;
            }
            
            Optional<Report> latestReport = reportRepository.findTopByDeviceOrderByRepairDateDesc(log.getMainDevice());
            LocalDateTime waktuDown = log.getWaktu()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
            
            if (latestReport.isPresent()) {
                LocalDateTime repairDate = latestReport.get().getRepairDate();
                // Validasi repair date: tidak null, tidak sebelum waktu down, dan tidak di masa depan
                if (repairDate != null && !repairDate.isBefore(waktuDown) && !repairDate.isAfter(now)) {
                    log.setRepairTime(repairDate);
                    failOverLogRepository.save(log);
                    continue;
                }
            }

            long seed = log.getMainDevice().getId(); 
            int minutesToAdd = 30 + new Random(seed).nextInt(90);
            LocalDateTime estimatedTime = waktuDown.plusMinutes(minutesToAdd);
            
            log.setRepairTime(estimatedTime);
            failOverLogRepository.save(log); // Simpan ke database
        }

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

        data.put("latestLogs", logPageResult.getContent());
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
