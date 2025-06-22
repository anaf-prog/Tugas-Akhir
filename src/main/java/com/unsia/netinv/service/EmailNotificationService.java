package com.unsia.netinv.service;

import com.unsia.netinv.entity.Device;

public interface EmailNotificationService {
    void sendDeviceDownNotification(Device device, String additionalMessage);
    void sendMaintenanceNotification(Device device, String message);
    void sendMaintenanceEndNotification(Device device, String message);
}
