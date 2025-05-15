package com.unsia.netinv.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.BackupRoutes;
import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.Location;
import com.unsia.netinv.repository.BackupRouteRepository;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.LocationRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class DeviceServiceImpl implements Deviceservice {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    BackupRouteRepository backupRouteRepository;

    @Override
    public boolean isIpAddressDuplicate(String ipAddress) {
        return deviceRepository.existsByIpAddress(ipAddress);
    }

    @Override
    public Device createdeviceWithlocation(Device device, String locationName, String building, String floor, String room) {
        Location location = new Location();
        location.setLocationName(locationName);
        location.setBuilding(building);
        location.setFloor(floor);
        location.setRoom(room);
        Location savedLocation = locationRepository.save(location);

        device.setLocation(savedLocation);
        device.setLastChecked(new Date());

        return deviceRepository.save(device);
    }

    @Override
    public Device prepareDefaultDevice() {
        Device device = new Device();
        device.setStatusDevice("ONLINE");
        device.setPingStatus("ONLINE");
        device.setDeviceType("ROUTER");
        return device;
    }

    @Override
    public Device getDeviceByIdOrThrow(Long id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Perangkat tidak ditemukan"));
    }

    @Override
    public List<Device> getAvailableDeviceExcluding(Long excludingId) {
        return deviceRepository.findAll().stream()
            .filter(d -> !d.getId().equals(excludingId))
            .collect(Collectors.toList());
    }

    @Override
    public List<Device> getCurrentbackupDevice(Device device) {
        return backupRouteRepository.findByMainDevice(device).stream()
            .map(BackupRoutes::getBackupDevice)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isIpAddressDuplicate(String ipAddress, Long currentDeviceId) {
        Device existing = deviceRepository.findByIpAddress(ipAddress);
        return existing != null && !existing.getId().equals(currentDeviceId);
    }

    @Override
    public void updateDeviceAndLocation(Device deviceToUpdate, Device updateDevice, String locationName, String building, String floor, String room) {
        deviceToUpdate.setDeviceName(updateDevice.getDeviceName());
        deviceToUpdate.setIpAddress(updateDevice.getIpAddress());
        deviceToUpdate.setMacAddress(updateDevice.getMacAddress());
        deviceToUpdate.setStatusDevice(updateDevice.getStatusDevice());
        deviceToUpdate.setPingStatus(updateDevice.getPingStatus());
        deviceToUpdate.setLastChecked(new Date());

        Location location = deviceToUpdate.getLocation() != null ? deviceToUpdate.getLocation() : new Location();

        location.setLocationName(locationName);
        location.setBuilding(building);
        location.setFloor(floor);
        location.setRoom(room);

        location = locationRepository.save(location);
        deviceToUpdate.setLocation(location);

        deviceRepository.save(deviceToUpdate);
    }

    @Override
    public void updateBackupDevices(Device mainDevice, List<Long> backupdeviceIds) {
        backupRouteRepository.deleteByMainDevice(mainDevice);
        backupRouteRepository.flush();

        if (backupdeviceIds != null && !backupdeviceIds.isEmpty()) {
            for (Long backupDeviceId : backupdeviceIds) {
                Device backupDevice = deviceRepository.findById(backupDeviceId)
                        .orElseThrow(() -> new IllegalArgumentException("Backup device tidak ditemukan"));

                BackupRoutes backupRoute = new BackupRoutes();
                backupRoute.setMainDevice(mainDevice);
                backupRoute.setBackupDevice(backupDevice);
                backupRoute.setIsActive(true);

                backupRouteRepository.save(backupRoute);
            }
        }
    }

    @Override
    public void deleteDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Perangkat tidak ditemukan"));

        deviceRepository.delete(device);
    }
    
}
