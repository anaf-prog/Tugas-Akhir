package com.unsia.netinv.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unsia.netinv.service.SimulationService;

@RestController
@RequestMapping("/api/simulation")
public class FailoverController {

    @Autowired
    SimulationService simulation;

    @PostMapping("/schedule-failure")
    public String scheduleFailure(@RequestParam String ipAddress,
                                  @RequestParam int minutesToFailure) {

        simulation.scheduleFailure(ipAddress, minutesToFailure);
        return "Failure scheduled for " + ipAddress + " in " + minutesToFailure + " minutes";
    }

    @PostMapping("/recover")
    public String recoveryDevice(@RequestParam(required = false) String ipAddress) {
        if (ipAddress != null) {
            simulation.recoveryDevice(ipAddress);
            return "Device " + ipAddress + " recovered";
        } else {
            simulation.recoveryAllDevices();
            return "All devices recovered";
        }

    }
    
    @GetMapping("failed-device")
    public Set<String> getFaileddevice() {
        return simulation.getFaileddevices();
    }
    
}
