package com.unsia.netinv.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unsia.netinv.dto.DeviceDropdown;
import com.unsia.netinv.repository.DeviceRepository;

@RestController
@RequestMapping("/api/devices")
public class DeviceApiController {
    
    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping
    public ResponseEntity<List<DeviceDropdown>> getAllDevices() {
        List<DeviceDropdown> devices = deviceRepository.findAll()
            .stream()
            .map(device -> new DeviceDropdown(device.getId(), device.getDeviceName()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(devices);
    }
}