package com.unsia.netinv.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unsia.netinv.dto.MaintenanceLogRequest;
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MaintenanceLog;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.netinve.LogReason;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MaintenanceLogRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;
import com.unsia.netinv.utility.MaintenanceScheduler;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceServiceImpl.class);

    @Autowired
    private MaintenanceScheduler maintenanceScheduler;

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringLogRepository monitoringLogRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Override
    public List<MaintenanceLog> getAllMaintenanceLogs() {
        return maintenanceLogRepository.findAllWithDevice();
    }

    @Override
    public ResponseEntity<MaintenanceLog> getMaintenanceLogById(Long id) {
        return maintenanceLogRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    public ResponseEntity<?> createMaintenanceLog(MaintenanceLogRequest request) {
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

            // Handle repair completion time
            if (request.getRepairCompletionTime() != null && !request.getRepairCompletionTime().isEmpty()) {
                LocalDateTime completionTime = LocalDateTime.parse(request.getRepairCompletionTime());
                maintenanceLog.setRepairCompletionTime(completionTime);

                if (maintenanceLog.getScheduledTime() != null && completionTime.isBefore(maintenanceLog.getScheduledTime())) {
                   return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("message", "Waktu selesai harus setelah waktu mulai maintenance")); 
                }
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

    @Override
    public ResponseEntity<?> enableDevice(Long id) {
        log.info("Attempting to enable device ID: {}", id);
        try {
            Optional<Device> deviceOpt = deviceRepository.findById(id);
            if (!deviceOpt.isPresent()) {
                log.warn("Device not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            Device device = deviceOpt.get();

            // Khusus jika sebelumnya MAINTENANCE, buat log MAINTENANCE_END
            if ("MAINTENANCE".equals(device.getStatusDevice())) {
                MonitoringLog endLog = new MonitoringLog();
                endLog.setDevice(device);
                endLog.setPingStatus(true);
                endLog.setMonitoring(new Date());
                endLog.setLogReason(LogReason.MAINTENANCE_END);
                monitoringLogRepository.save(endLog);
                
                // Kirim notifikasi maintenance selesai
                emailNotificationService.sendMaintenanceEndNotification(
                    device, "Pemeliharaan perangkat telah selesai");
            }

            device.setStatusDevice("ONLINE");
            device.setLastChecked(new Date());
            Device savedDevice = deviceRepository.saveAndFlush(device);
            
            // Create monitoring log
            MonitoringLog monitoringLog = new MonitoringLog();
            monitoringLog.setDevice(device);
            monitoringLog.setPingStatus(true);
            monitoringLog.setMonitoring(new Date());
            monitoringLogRepository.save(monitoringLog);
            
            log.info("Device successfully enabled. New status: {}", savedDevice.getStatusDevice());
            
            return ResponseEntity.ok(Map.of(
                "message", "Perangkat berhasil diaktifkan",
                "deviceId", savedDevice.getId(),
                "status", savedDevice.getStatusDevice()
            ));
        } catch (Exception e) {
            log.error("Error enabling device ID: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "Gagal mengaktifkan perangkat",
                "error", e.getMessage()
            ));
        }
    }

    @Override
    public ResponseEntity<?> disableDevice(Long id) {
        log.info("Attempting to disable device ID: {}", id);
        try {
            Optional<Device> deviceOpt = deviceRepository.findById(id);
            if (!deviceOpt.isPresent()) {
                log.warn("Device not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            Device device = deviceOpt.get();
            log.info("Device current status: {}", device.getStatusDevice());
            
            device.setStatusDevice("MAINTENANCE");
            device.setLastChecked(new Date());
            Device savedDevice = deviceRepository.save(device);
            
            // Create monitoring log
            MonitoringLog monitoringLog = new MonitoringLog();
            monitoringLog.setDevice(device);
            monitoringLog.setPingStatus(false);
            monitoringLog.setMonitoring(new Date());
            monitoringLog.setLogReason(LogReason.MAINTENANCE);
            monitoringLogRepository.save(monitoringLog);
            
            log.info("Device successfully set to maintenance. New status: {}", savedDevice.getStatusDevice());
            
            return ResponseEntity.ok(Map.of(
                "message", "Perangkat berhasil dinonaktifkan untuk pemeliharaan",
                "deviceId", savedDevice.getId(),
                "status", savedDevice.getStatusDevice()
            ));
        } catch (Exception e) {
            log.error("Error disabling device ID: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "Gagal menonaktifkan perangkat untuk pemeliharaan",
                "error", e.getMessage()
            ));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateMaintenanceLog(Long id, MaintenanceLogRequest request) {
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

    @Override
    @Transactional
    public ResponseEntity<?> deleteMaintenanceLog(Long id) {
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
