package com.unsia.netinv.service;

import java.util.Set;

public interface SimulationService {
    void init(); // kalau mau simulasi failover otomatis ini di aktifkan

    void scheduleFailure(String ipAddress, int minutesToFailure); // kalau mau simulasi failover otomatis ini di aktifkan

    void cancelScheduleFailure(String ipAddress);

    void recoveryDevice(String ipAddress);

    void recoveryAllDevices();

    void checkScheduleFailures(); // kalau mau simulasi failover otomatis ini di aktifkan

    Set<String> getFaileddevices();
}
