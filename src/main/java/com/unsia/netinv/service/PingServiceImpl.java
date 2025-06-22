package com.unsia.netinv.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.repository.DeviceRepository;


@Service
public class PingServiceImpl implements PingService {

    @Autowired
    private DeviceRepository deviceRepository;

    @SuppressWarnings("unused")
    private final Map<String, Boolean> deviceStatusSimulation = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Override
    public boolean pingDevice(String ipAddress) {
        // Selalu baca status terbaru dari database
        Device device = deviceRepository.findByIpAddress(ipAddress);
        if (device == null) {
            return false; // Jika device tidak ditemukan, anggap offline
        }
        return "ONLINE".equalsIgnoreCase(device.getStatusDevice());
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
        // Update database langsung
        Device device = deviceRepository.findByIpAddress(ipAddress);
        if (device != null) {
            device.setStatusDevice(shouldFail ? "OFFLINE" : "ONLINE");
            deviceRepository.save(device);
        }
    }
    
}
