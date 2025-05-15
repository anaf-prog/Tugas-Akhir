// package com.unsia.netinv.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.unsia.netinv.entity.Device;
// import com.unsia.netinv.repository.DeviceRepository;

// @Service
// public class SimulationServiceImpl implements SimulationService {

//     @Autowired
//     DeviceRepository deviceRepository;

//     @Autowired
//     FailoverService failoverService;

//     @Transactional
//     @Override
//     public void simulationDeviceFailure(Long deviceId) {
//         System.out.println("============= Start Simulation =====================");

//          // 1. Matikan perangkat utama
//         Device device = deviceRepository.findById(deviceId)
//             .orElseThrow(() -> new RuntimeException("Device not found!"));

//         System.out.println("[1. Main Device Initial] ID: " + device.getId() + " Status: " + device.getStatusDevice());
        
//         // 2. Verify database state
//         Device freshDevice = deviceRepository.getReferenceById(deviceId);
//         System.out.println("[2. Main Device Fresh From DB] Status: " + freshDevice.getStatusDevice());
 
//         device.setStatusDevice("OFFLINE");
//         Device savedDevice = deviceRepository.save(device);
//         System.out.println("[3. Main Device After Save] Status: " + savedDevice.getStatusDevice());

//         // 4. Verify again
//         Device updatedDevice = deviceRepository.findById(deviceId).get();
//         System.out.println("[4. Main Device After Refresh] Status: " + updatedDevice.getStatusDevice());

//         failoverService.activateBackupRoute(deviceId);
//         System.out.println("======== END SIMULATION ========");
//     }
    
// }
