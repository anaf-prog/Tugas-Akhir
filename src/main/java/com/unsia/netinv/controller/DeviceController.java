package com.unsia.netinv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.service.Deviceservice;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/devices")
@Transactional
public class DeviceController {

    @Autowired
    Deviceservice deviceservice;

    @GetMapping("/add")
    public String showAddDevice(Model model) {
        model.addAttribute("device", deviceservice.prepareDefaultDevice());
        return "add-device";

    }

    @PostMapping("/add")
    public String addDeice(@Valid @ModelAttribute Device device,
                           BindingResult result,
                           @RequestParam(required = false) String locationName,
                           @RequestParam(required = false) String building,
                           @RequestParam(required = false) String floor,
                           @RequestParam(required = false) String room,
                           RedirectAttributes redirectAttributes) {

        try {
            if (deviceservice.isIpAddressDuplicate(device.getIpAddress())) {
            result.rejectValue("ipAddress", "duplicate", "IP Address sudah digunakan");
            }

            if (result.hasErrors()) {
                return "add-device";
            }

            deviceservice.createdeviceWithlocation(device, locationName, building, floor, room);

            redirectAttributes.addFlashAttribute("successMessage", "Perangkat berhasil ditambahkan");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menambah data perangkat");
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String showEditDeviceForm(@PathVariable("id") Long id, Model model) {
        Device device = deviceservice.getDeviceByIdOrThrow(id);
        List<Device> availableDevices = deviceservice.getAvailableDeviceExcluding(id);
        List<Device> currentBackupDevices = deviceservice.getCurrentbackupDevice(device);

        model.addAttribute("device", device);
        model.addAttribute("availableDevices", availableDevices);
        model.addAttribute("currentBackupDevices", currentBackupDevices);

        if (device.getLocation() != null) {
            model.addAttribute("locationname", device.getLocation().getLocationName());
            model.addAttribute("building", device.getLocation().getBuilding());
            model.addAttribute("floor", device.getLocation().getFloor());
            model.addAttribute("room", device.getLocation().getRoom());
        }

        return "edit-device";
    }

    @PostMapping("/update/{id}")
    public String updateDevice(@PathVariable("id") Long id,
                               @Valid @ModelAttribute Device device,
                               BindingResult result,
                               @RequestParam(required = false) String locationName,
                               @RequestParam(required = false) String building,
                               @RequestParam(required = false) String floor,
                               @RequestParam(required = false) String room,
                               @RequestParam(required = false) List<Long> backupDevices,
                               RedirectAttributes redirectAttributes) {

        try {
            if (deviceservice.isIpAddressDuplicate(device.getIpAddress(), id)) {
            result.rejectValue("ipAddress", "duplicate", "IP Address sudah digunakan");
            }

            if (result.hasErrors()) {
                return "edit-devices";
            }

            Device deviceToUpdate = deviceservice.getDeviceByIdOrThrow(id);
            deviceservice.updateDeviceAndLocation(deviceToUpdate, device, locationName, building, floor, room);
            deviceservice.updateBackupDevices(deviceToUpdate, backupDevices);

            redirectAttributes.addFlashAttribute("successMessage", "Data perangkat berhasil diperbarui");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal update perangkat");
        }

        return "redirect:/dashboard";
                            
    }

    @GetMapping("/delete/{id}")
    public String deleteDevice(@PathVariable("id") long id,
                               RedirectAttributes redirectAttributes) {

        try {
            deviceservice.deleteDeviceById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Perangkat berhasil dihapus");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus perangkat");
        }

        return "redirect:/dashboard";
    }
}
