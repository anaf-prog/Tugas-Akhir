package com.unsia.netinv.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @Override
    public Map<String, Object> getDashboardData(int devicePage, int deviceSize, int logPage, int logSize, Users user) {
        Map<String, Object> data = new HashMap<>();

        long totalDevices = deviceRepository.count();
        long activeDevices = deviceRepository.countByStatusDevice("ONLINE");
        long downDevices = deviceRepository.countByStatusDevice("OFFLINE");

        long routerCount = deviceRepository.countByDeviceType("ROUTER");
        long switchCount = deviceRepository.countByDeviceType("SWITCH");
        long serverCount = deviceRepository.countByDeviceType("SERVER");

        Pageable devicPageable = PageRequest.of(devicePage, deviceSize, Sort.by("deviceName").ascending());
        Page<Device> devicePageResult = deviceRepository.findAll(devicPageable);

        Pageable logPageable = PageRequest.of(logPage, logSize, Sort.by("monitoring").descending());
        Page<MonitoringLog> logPageResult = monitoringLogRepository.findLatestLogPerDevice(logPageable);

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
