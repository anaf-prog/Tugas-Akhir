package com.unsia.netinv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unsia.netinv.dto.MaintenanceLogRequest;
import com.unsia.netinv.entity.MaintenanceLog;
import com.unsia.netinv.service.MaintenanceService;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceApiController {

    @Autowired
    private MaintenanceService maintenanceService;

    @GetMapping
    public List<MaintenanceLog> getAllMaintenanceLogs() {
        return maintenanceService.getAllMaintenanceLogs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceLog> getMaintenanceLogById(@PathVariable Long id) {
        return maintenanceService.getMaintenanceLogById(id);
    }

    @PostMapping
    public ResponseEntity<?> createMaintenanceLog(@RequestBody MaintenanceLogRequest request) {
        return maintenanceService.createMaintenanceLog(request);
    }

    @PutMapping("/device/{id}/enable")
    public ResponseEntity<?> enableDevice(@PathVariable Long id) {
        return maintenanceService.enableDevice(id);
    }

    @PutMapping("/device/{id}/disable")
    public ResponseEntity<?> disableDevice(@PathVariable Long id) {
        return maintenanceService.disableDevice(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaintenanceLog(@PathVariable Long id, @RequestBody MaintenanceLogRequest request) {
        return maintenanceService.updateMaintenanceLog(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMaintenanceLog(@PathVariable Long id) {
        return maintenanceService.deleteMaintenanceLog(id);
    }
    
}
