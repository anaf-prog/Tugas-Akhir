// package com.unsia.netinv.controller;

// import java.text.SimpleDateFormat;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.unsia.netinv.entity.MonitoringLog;
// import com.unsia.netinv.repository.MonitoringLogRepository;

// @RestController
// @RequestMapping("/api/ping")
// public class PingStatusController {

//     @Autowired
//     private MonitoringLogRepository monitoringLogRepository;

//     @GetMapping("/status")
//     public ResponseEntity<List<Map<String, Object>>> getPingStatus() {
//         // Ambil log terbaru untuk setiap device
//         List<MonitoringLog> latestLogs = monitoringLogRepository.findLatestLogForEachDevice();
        
//         List<Map<String, Object>> result = latestLogs.stream().map(log -> {
//             Map<String, Object> map = new HashMap<>();
//             map.put("deviceId", log.getDevice().getId());
//             map.put("isActive", log.getPingStatus());
            
//             // Cek apakah sedang dalam proses ping (30 detik terakhir)
//             Date now = new Date();
//             long diffInMillis = now.getTime() - log.getMonitoring().getTime();
//             map.put("isPinging", diffInMillis <= 30000); // 30 detik
            
//             map.put("lastPingTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(log.getMonitoring()));
            
//             return map;
//         }).collect(Collectors.toList());
        
//         return ResponseEntity.ok(result);
//     }
// }
