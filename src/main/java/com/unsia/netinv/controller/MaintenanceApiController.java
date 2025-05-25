package com.unsia.netinv.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MaintenanceLog;
import com.unsia.netinv.repository.MaintenanceLogRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceApiController {

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    @GetMapping
    public List<MaintenanceLog> getAllMaintenanceLogs() {
        return maintenanceLogRepository.findAllWithDevice();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceLog> getMaintenanceLogById(@PathVariable Long id) {
        return maintenanceLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMaintenanceLog(@RequestBody MaintenanceLogRequest request, HttpServletRequest servletRequest) {
        try {
            MaintenanceLog maintenanceLog = new MaintenanceLog();
            
            Device device = new Device();
            device.setId(request.getDevice().getId());
            maintenanceLog.setDevice(device);
            
            maintenanceLog.setMaintenanceDate(LocalDateTime.parse(request.getMaintenanceDate()));
            maintenanceLog.setTechnician(request.getTechnician());
            maintenanceLog.setDescription(request.getDescription());
            
            MaintenanceLog savedLog = maintenanceLogRepository.save(maintenanceLog);
            
            // Return success dengan message
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Data pemeliharaan berhasil ditambahkan");
            response.put("data", savedLog);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Gagal menambahkan data pemeliharaan");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaintenanceLog(@PathVariable Long id, @RequestBody MaintenanceLogRequest request) {
        try {
            Optional<MaintenanceLog> existingLog = maintenanceLogRepository.findById(id);
            if (!existingLog.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            MaintenanceLog logToUpdate = existingLog.get();
            
            // Update data
            Device device = new Device();
            device.setId(request.getDevice().getId());
            logToUpdate.setDevice(device);
            
            logToUpdate.setMaintenanceDate(LocalDateTime.parse(request.getMaintenanceDate()));
            logToUpdate.setTechnician(request.getTechnician());
            logToUpdate.setDescription(request.getDescription());

            MaintenanceLog updatedLog = maintenanceLogRepository.save(logToUpdate);
            
            return ResponseEntity.ok(Map.of(
                "message", "Data pemeliharaan berhasil diperbarui",
                "data", updatedLog
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Gagal memperbarui data",
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMaintenanceLog(@PathVariable Long id) {
        if (!maintenanceLogRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            maintenanceLogRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Data pemeliharaan berhasil dihapus");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Gagal menghapus data pemeliharaan");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
}
