package com.unsia.netinv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.netinve.LogReason;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;

@Service
public class NetworkMonitoringServiceImpl implements NetworkMonitoringService {
    private static final Logger logger = LoggerFactory.getLogger(NetworkMonitoringServiceImpl.class);

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @Autowired
    private PingService pingService;

    @Autowired
    private FailoverService failoverService;

    @Override
    @Scheduled(fixedRate = 60000) 
    public void monitoringAlldevices() {
        List<Device> devices = deviceRepository.findAll();
        logger.info("Starting monitoring for {} devices ...", devices.size());

        for (Device device : devices) {
            try {
                boolean isOnline = pingService.pingDevice(device.getIpAddress());
                Long responseTime = pingService.pingWithResponseTime(device.getIpAddress());
                String newStatus = isOnline ? "ONLINE" : "OFFLINE";
                boolean statusChanged = !device.getStatusDevice().equalsIgnoreCase(newStatus);
                boolean isAnomaly = isResponseTimeAnomaly(responseTime);
                
                if (statusChanged) {
                    device.setStatusDevice(newStatus);
                    deviceRepository.save(device);

                    if (!isOnline) {
                        try {
                            failoverService.activateBackupRoute(device.getId());
                        } catch (Exception e) {
                            logger.error("Gagal failover untuk perangkat ", device.getDeviceName(), e.getMessage());
                        }
                    }
                }

                if (statusChanged || isAnomaly) {
                    createNewLog(device, isOnline, responseTime, statusChanged ? LogReason.STATUS_CHANGE : LogReason.ANOMALY);
                } else {
                    updateLastLog(device, isOnline, responseTime);
                }

                logger.info("Device {} ({}): Status {} - Response Time {} ms",
                    device.getDeviceName(), device.getIpAddress(), 
                    newStatus,
                    responseTime != null ? responseTime : "N/A");

            } catch (Exception e) {
                logger.error("Error monitoring device {}: {}", device.getDeviceName(), e.getMessage());
            }
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

        logger.debug("Created NEW log for device {} with reason {}", device.getDeviceName(), reason);
    }

    private void updateLastLog(Device device, boolean isOnline, Long responseTime) {
        Pageable latest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "monitoring"));
        Optional<MonitoringLog> lastLogOpt = monitoringLogRepository.findTopByDeviceOrderByMonitoringDesc(device);

        System.out.println("Latest : " + latest);
        
        if (lastLogOpt.isPresent()) {
            MonitoringLog lastLog = lastLogOpt.get();
            // Update log yang ada jika reason-nya REGULAR_UPDATE
            if (lastLog.getLogReason() == LogReason.REGULAR_UPDATE) {
                lastLog.setPingStatus(isOnline);
                lastLog.setResponseTime(responseTime != null ? responseTime.intValue() : null);
                lastLog.setMonitoring(new Date());
                monitoringLogRepository.save(lastLog);
                logger.debug("UPDATED existing log for device {}", device.getDeviceName());
            } else {
                // Buat log baru jika log terakhir bukan REGULAR_UPDATE
                createNewLog(device, isOnline, responseTime, LogReason.REGULAR_UPDATE);
            }
        } else {
            // Buat log baru jika tidak ada log sama sekali
            createNewLog(device, isOnline, responseTime, LogReason.REGULAR_UPDATE);
        }
    }

    @Override
    public List<Device> checkFailedDevices() {
        return deviceRepository.findByStatusDevice("OFFLINE");
    }

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
}
