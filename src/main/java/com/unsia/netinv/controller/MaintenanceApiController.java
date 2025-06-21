package com.unsia.netinv.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MaintenanceLogRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;
import com.unsia.netinv.utility.MaintenanceScheduler;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceApiController {

    private final MaintenanceScheduler maintenanceScheduler;
    private static final Logger log = LoggerFactory.getLogger(MaintenanceApiController.class);

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    MaintenanceApiController(MaintenanceScheduler maintenanceScheduler) {
        this.maintenanceScheduler = maintenanceScheduler;
    }

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
    public ResponseEntity<?> createMaintenanceLog(@RequestBody MaintenanceLogRequest request) {
        try {
            // Validasi input
            if (request.getDeviceId() == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Perangkat harus dipilih"));
            }

            Optional<Device> deviceOpt = deviceRepository.findById(request.getDeviceId());
            if (!deviceOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Perangkat tidak ditemukan"));
            }

            // Validasi tanggal
            if (request.getMaintenanceDate() == null || request.getMaintenanceDate().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Tanggal maintenance harus diisi"));
            }

            // Buat maintenance log tanpa relasi yang tidak perlu
            MaintenanceLog maintenanceLog = new MaintenanceLog();
            maintenanceLog.setDevice(deviceOpt.get());
            maintenanceLog.setMaintenanceDate(LocalDateTime.parse(request.getMaintenanceDate()));
            maintenanceLog.setTechnician(request.getTechnician());
            maintenanceLog.setDescription(request.getDescription());

            // Handle scheduled maintenance
            if (request.getScheduledTime() != null && !request.getScheduledTime().isEmpty()) {
                LocalDateTime scheduledTime = LocalDateTime.parse(request.getScheduledTime());
                maintenanceLog.setScheduledTime(scheduledTime);
                maintenanceLog.setAutoDisable(request.getAutoDisable() != null && request.getAutoDisable());
                
                log.info("Setting scheduled time to: {}", scheduledTime);
            }

            // Simpan dan dapatkan response sederhana
            MaintenanceLog savedLog = maintenanceLogRepository.save(maintenanceLog);

            // Schedule jika perlu
            if (savedLog.getScheduledTime() != null && savedLog.getAutoDisable()) {
                maintenanceScheduler.scheduleDeviceDisable(savedLog);
            }

            // Return response sederhana tanpa nested object
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Data pemeliharaan berhasil ditambahkan");
            response.put("id", savedLog.getId());
            return ResponseEntity.ok(response);

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Format tanggal tidak valid"));
        } catch (Exception e) {
            log.error("Error creating maintenance log", e);
            return ResponseEntity.internalServerError().body(Collections.singletonMap("message", "Terjadi kesalahan server"));
        }
    }

    @PutMapping("/device/{id}/disable")
    public ResponseEntity<?> disableDevice(@PathVariable Long id) {
        log.info("Attempting to disable device ID: {}", id);
        try {
            Optional<Device> deviceOpt = deviceRepository.findById(id);
            if (!deviceOpt.isPresent()) {
                log.warn("Device not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            Device device = deviceOpt.get();
            log.info("Device current status: {}", device.getStatusDevice());
            
            device.setStatusDevice("OFFLINE");
            device.setLastChecked(new Date());
            Device savedDevice = deviceRepository.save(device);
            
            // Create monitoring log
            MonitoringLog monitoringLog = new MonitoringLog();
            monitoringLog.setDevice(device);
            monitoringLog.setPingStatus(false);
            monitoringLog.setMonitoring(new Date());
            monitoringLogRepository.save(monitoringLog);
            
            log.info("Device successfully disabled. New status: {}", savedDevice.getStatusDevice());
            
            return ResponseEntity.ok(Map.of(
                "message", "Perangkat berhasil dinonaktifkan",
                "deviceId", savedDevice.getId(),
                "status", savedDevice.getStatusDevice()
            ));
        } catch (Exception e) {
            log.error("Error disabling device ID: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "Gagal menonaktifkan perangkat",
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMaintenanceLog(@PathVariable Long id, @RequestBody MaintenanceLogRequest request) {
        try {
            Optional<MaintenanceLog> existingLog = maintenanceLogRepository.findById(id);
            if (!existingLog.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "message", "Data maintenance tidak ditemukan",
                        "error", "Maintenance log with ID " + id + " not found"
                    ));
            }

            Optional<Device> deviceOptional = deviceRepository.findById(request.getDeviceId());
            if (!deviceOptional.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "message", "Device tidak ditemukan",
                        "error", "Device with ID " + request.getDeviceId() + " not found"
                    ));
            }
            
            MaintenanceLog logToUpdate = existingLog.get();
            logToUpdate.setDevice(deviceOptional.get());
            
            try {
                logToUpdate.setMaintenanceDate(LocalDateTime.parse(request.getMaintenanceDate()));
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "message", "Format tanggal tidak valid",
                        "error", "Invalid date format. Expected format: yyyy-MM-dd'T'HH:mm:ss"
                    ));
            }
            
            logToUpdate.setTechnician(request.getTechnician());
            logToUpdate.setDescription(request.getDescription());

            MaintenanceLog updatedLog = maintenanceLogRepository.save(logToUpdate);
            
            // Buat response DTO manual untuk menghindari masalah serialisasi
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Data pemeliharaan berhasil diperbarui");
            response.put("data", Map.of(
                "id", updatedLog.getId(),
                "maintenanceDate", updatedLog.getMaintenanceDate().toString(),
                "technician", updatedLog.getTechnician(),
                "description", updatedLog.getDescription(),
                "deviceId", updatedLog.getDevice().getId()
            ));
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
            
        } catch (Exception e) {
            log.error("Error updating maintenance log", e);
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                    "message", "Gagal memperbarui data",
                    "error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred"
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
