package com.unsia.netinv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.MonitoringLog;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.repository.DeviceRepository;
import com.unsia.netinv.repository.MonitoringLogRepository;
import com.unsia.netinv.service.NetworkMonitoringService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    MonitoringLogRepository monitoringLogRepository;

    @Autowired
    NetworkMonitoringService networkMonitoringService;

    @GetMapping
    public String showDasboard(@RequestParam(value = "devicePage", defaultValue = "0") int devicePage,
                               @RequestParam(value = "deviceSize", defaultValue = "5") int deviceSize,
                               @RequestParam(value = "logPage", defaultValue = "0") int logPage,
                               @RequestParam(value = "logSize", defaultValue = "5") int logSize,
                               Model model, 
                               HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        long totalDevices = deviceRepository.count();
        long activeDevices = deviceRepository.countByStatusDevice("ONLINE");
        long downDevices = deviceRepository.countByStatusDevice("OFFLINE");

        Pageable devicPageable = PageRequest.of(devicePage, deviceSize, Sort.by("deviceName").ascending());
        Page<Device> devicePageResult = deviceRepository.findAll(devicPageable);

        Pageable logPageable = PageRequest.of(logPage, logSize, Sort.by("monitoring").descending());
        Page<MonitoringLog> logPageResult = monitoringLogRepository.findLatestLogPerDevice(logPageable);
        List<MonitoringLog> latestLogs = logPageResult.getContent();

        model.addAttribute("currentUser", user.getUsername());
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("activeDevices", activeDevices);
        model.addAttribute("downDevices", downDevices);

        model.addAttribute("devices", devicePageResult.getContent());
        model.addAttribute("deviceCurrentPage", devicePage);
        model.addAttribute("deviceTotalPages", devicePageResult.getTotalPages());
        model.addAttribute("totalItems", devicePageResult.getTotalElements());

        model.addAttribute("latestLogs", latestLogs);
        model.addAttribute("logCurrentPage", logPage);
        model.addAttribute("logTotalPages", logPageResult.getTotalPages());
        model.addAttribute("logTotalItems", logPageResult.getTotalElements());

        return "dashboard";

    }
}
