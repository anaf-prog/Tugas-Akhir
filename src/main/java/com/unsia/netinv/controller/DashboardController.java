package com.unsia.netinv.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.unsia.netinv.entity.Users;
import com.unsia.netinv.service.DashboardService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public String showDasboard(@RequestParam(value = "devicePage", defaultValue = "0") int devicePage,
                               @RequestParam(value = "deviceSize", defaultValue = "5") int deviceSize,
                               @RequestParam(value = "logPage", defaultValue = "0") int logPage,
                               @RequestParam(value = "logSize", defaultValue = "5") int logSize,
                               @RequestParam(value = "search", required = false) String search,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "type", required = false) String type,
                               Model model, 
                               HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Map<String, Object> dashboardData = dashboardService.getDashboardData(devicePage, deviceSize, logPage, logSize, search, status, type, user);
        model.addAllAttributes(dashboardData);

        model.addAttribute("searchParam", search);
        model.addAttribute("statusParam", status);
        model.addAttribute("typeParam", type);

        return "dashboard";

    }

    @GetMapping("/devices/down")
    public ResponseEntity<List<Map<String, Object>>> getDownDevices() {
        List<Map<String, Object>> response = dashboardService.getDownDevices();
        return ResponseEntity.ok(response);
    }
}
