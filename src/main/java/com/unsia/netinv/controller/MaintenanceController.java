package com.unsia.netinv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.unsia.netinv.entity.MaintenanceLog;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.repository.MaintenanceLogRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceLogRepository maintenanceLogRepository;

    @GetMapping
    public String showPerangkatPage(@RequestParam(value = "success", required = false) String success,
                                    @RequestParam(value = "error", required = false) String error,
                                    Model model, 
                                    HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (success != null) {
            model.addAttribute("successMessage", success);
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        List<MaintenanceLog> maintenanceLogs = maintenanceLogRepository.findAllWithDevice();

        model.addAttribute("currentUser", user.getUsername());
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("maintenanceLogs", maintenanceLogs);
        
        return "maintenance";
    }
    
}
