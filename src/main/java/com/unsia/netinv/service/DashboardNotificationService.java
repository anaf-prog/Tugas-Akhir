package com.unsia.netinv.service;

import com.unsia.netinv.entity.Device;

public interface DashboardNotificationService {
    void sendDeviceDownNotification(Device device, String Message);
    void sendDeviceRecoveredNotification(Device device, String message);
}
