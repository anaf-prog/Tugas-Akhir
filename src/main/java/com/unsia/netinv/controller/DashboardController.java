package com.unsia.netinv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String showDasboard(Model model, HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        long totalDevices = deviceRepository.count();
        long activeDevices = deviceRepository.countByStatusDevice("ONLINE");
        long downDevices = deviceRepository.countByStatusDevice("OFFLINE");

        List<Device> devices = deviceRepository.findAll();
        List<MonitoringLog> latestLogs = networkMonitoringService.getLatestMonitoringLogs(10);

        model.addAttribute("currentUser", user.getUsername());
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("activeDevices", activeDevices);
        model.addAttribute("downDevices", downDevices);
        model.addAttribute("devices", devices);
        model.addAttribute("latestLogs", latestLogs);

        return "dashboard";

    }
}
