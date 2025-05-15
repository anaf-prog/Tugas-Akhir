package com.unsia.netinv.service;

public interface FailoverService {
    void activateBackupRoute(Long mainDeviceId);
}
