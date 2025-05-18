package com.unsia.netinv.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.service.SimulationService;

@RestController
@RequestMapping("/api/simulation")
public class FailoverController {

    @Autowired
    private SimulationService simulation;

    @Autowired
    private DeviceRepository deviceRepository;

    // @PostMapping("/schedule-failure")
    // public String scheduleFailure(@RequestParam String ipAddress,
    //                               @RequestParam int minutesToFailure) {

    //     simulation.scheduleFailure(ipAddress, minutesToFailure);
    //     return "Failure scheduled for " + ipAddress + " in " + minutesToFailure + " minutes";
    // }

    @PostMapping("/recover")
    public ResponseEntity<Map<String, Object>> recoverydevice(@RequestParam String ipAddress) {
        Map<String, Object> response = new HashMap<>();

        try {
            Device device = deviceRepository.findByIpAddress(ipAddress);
            if (device == null) {
                response.put("success", false);
                response.put("message", "Device dengan IP " + ipAddress + " tidak ditemukan");
                return ResponseEntity.badRequest().body(response);
            }

            simulation.recoveryDevice(ipAddress);

            response.put("success", true);
            response.put("message", "Perangkat " + device.getDeviceName() + " berhasil direcovery");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal melakukan recovery : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }
    
    @GetMapping("failed-device")
    public Set<String> getFaileddevice() {
        return simulation.getFaileddevices();
    }
    
}
