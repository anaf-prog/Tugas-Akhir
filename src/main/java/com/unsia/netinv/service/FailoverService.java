package com.unsia.netinv.service;

import java.util.Map;

import com.unsia.netinv.entity.MonitoringLog;

public interface FailoverService {
    void activateBackupRoute(Long mainDeviceId);

    boolean isBackupActive(Long backupDeviceId);

    Map<String, String> getDeviceStatusInfo(MonitoringLog log);
}
