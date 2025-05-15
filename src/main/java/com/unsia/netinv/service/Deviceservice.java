package com.unsia.netinv.service;

import java.util.List;

import com.unsia.netinv.entity.Device;

public interface Deviceservice {
    boolean isIpAddressDuplicate(String ipAddress);

    Device createdeviceWithlocation(Device device, String locationName, String building, String floor, String room);

    Device prepareDefaultDevice();

    Device getDeviceByIdOrThrow(Long id);

    List<Device> getAvailableDeviceExcluding(Long excludingId);

    List<Device> getCurrentbackupDevice(Device device);

    boolean isIpAddressDuplicate(String ipAddress, Long currentDeviceId);

    void updateDeviceAndLocation(Device deviceToUpdate, Device updateDevice, String locationName, String building, String floor, String room);

    void updateBackupDevices(Device mainDevice, List<Long> backupdeviceIds);

    void deleteDeviceById(Long id);
}
