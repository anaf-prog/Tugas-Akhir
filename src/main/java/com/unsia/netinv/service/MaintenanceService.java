package com.unsia.netinv.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.unsia.netinv.dto.MaintenanceLogRequest;
import com.unsia.netinv.entity.MaintenanceLog;

public interface MaintenanceService {
    List<MaintenanceLog> getAllMaintenanceLogs();
    ResponseEntity<MaintenanceLog> getMaintenanceLogById(Long id);
    ResponseEntity<?> createMaintenanceLog(MaintenanceLogRequest request);
    ResponseEntity<?> enableDevice(Long id);
    ResponseEntity<?> disableDevice(Long id);
    ResponseEntity<?> updateMaintenanceLog(Long id, MaintenanceLogRequest request);
    ResponseEntity<?> deleteMaintenanceLog(Long id);
}
