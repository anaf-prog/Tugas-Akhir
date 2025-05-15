package com.unsia.netinv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.service.EmailNotificationService;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailNotificationService emailNotificationService;

    @PostMapping("/send")
    public String sendTestEmail() {
        Device device = new Device();
        device.setDeviceName("Test Device");
        device.setDeviceType("Router");
        device.setIpAddress("10.0.0.1");
        
        emailNotificationService.sendDeviceDownNotification(device, "Ini test otomatis");
        
        return "Test email sent!";
    }
    
}
