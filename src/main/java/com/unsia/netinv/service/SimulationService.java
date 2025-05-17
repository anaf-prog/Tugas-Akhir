package com.unsia.netinv.service;

import java.util.Set;

public interface SimulationService {
    void init();

    void scheduleFailure(String ipAddress, int minutesToFailure);

    void cancelScheduleFailure(String ipAddress);

    void recoveryDevice(String ipAddress);

    void recoveryAllDevices();

    void checkScheduleFailures();

    Set<String> getFaileddevices();
}
