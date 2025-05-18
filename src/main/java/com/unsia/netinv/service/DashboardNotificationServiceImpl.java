package com.unsia.netinv.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;

@Service
public class DashboardNotificationServiceImpl implements DashboardNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendDeviceDownNotification(Device device, String message) {
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("type", "DEVICE_DOWN");
        notification.put("deviceId", device.getId());
        notification.put("deviceName", device.getDeviceName());
        notification.put("ipAddress", device.getIpAddress());
        notification.put("message", message);
        notification.put("timestamp", new Date());

        messagingTemplate.convertAndSend("/topic/notifications", notification);

    }

    @Override
    public void sendDeviceRecoveredNotification(Device device, String message) {
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("type", "DEVICE_RECOVERED");
        notification.put("deviceId", device.getId());
        notification.put("deviceName", device.getDeviceName());
        notification.put("ipAddress", device.getIpAddress());
        notification.put("message", message);
        notification.put("timestamp", new Date());

        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
    
}
