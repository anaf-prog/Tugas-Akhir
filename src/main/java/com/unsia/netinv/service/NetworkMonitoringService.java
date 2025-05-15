package com.unsia.netinv.service;

import java.util.List;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MonitoringLog;

public interface NetworkMonitoringService {
    List<Device> checkFailedDevices();

    void monitoringAlldevices();

    List<MonitoringLog> getLatestMonitoringLogs(int count);
}
