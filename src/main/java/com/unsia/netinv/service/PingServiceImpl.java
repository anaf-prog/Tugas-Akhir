package com.unsia.netinv.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class PingServiceImpl implements PingService {

    private final Map<String, Boolean> deviceStatusSimulation = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public void init() {
        // Inisialisasi status perangkat dummy
        deviceStatusSimulation.put("192.168.1.1", true);
        deviceStatusSimulation.put("192.168.1.2", true);  
        deviceStatusSimulation.put("192.168.1.3", true);  
        deviceStatusSimulation.put("192.168.1.4", true);  
        deviceStatusSimulation.put("192.168.1.5", true);  
        deviceStatusSimulation.put("192.168.1.6", true);  
        deviceStatusSimulation.put("192.168.1.7", true);  
        
        deviceStatusSimulation.put("192.168.1.6", random.nextDouble() > 0.2);
        deviceStatusSimulation.put("192.168.1.5", random.nextDouble() > 0.1); 
    }

    @Override
    public boolean pingDevice(String ipAddress) {
        return deviceStatusSimulation.getOrDefault(ipAddress, true);
    }

    @Override
    public Long pingWithResponseTime(String ipAddress) {
        boolean isOnline = pingDevice(ipAddress);
        if (isOnline) {
            return (long) (random.nextInt(100) + 1);
        }
        return null;
    }

    @Override
    public void forceFailure(String ipAddress, boolean shouldFail) {
        deviceStatusSimulation.put(ipAddress, !shouldFail);
    }
    
}
