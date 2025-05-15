// package com.unsia.netinv.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.unsia.netinv.service.SimulationService;

// @RestController
// @RequestMapping("/api/failover")
// public class FailoverController {

//     @Autowired
//     SimulationService simulationService;

//     @PostMapping("/simulation")
//     public ResponseEntity<String> simulationFailure(@RequestParam Long deviceId) {
//         simulationService.simulationDeviceFailure(deviceId);
//         return ResponseEntity.ok("Failover simulation triggered for device: " + deviceId);
//     }
    
// }
