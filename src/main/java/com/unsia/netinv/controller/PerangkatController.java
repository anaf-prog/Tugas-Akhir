package com.unsia.netinv.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.repository.DeviceRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/perangkat")
public class PerangkatController {

    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping
    public String showPerangkatPage(Model model, HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Device> devices = deviceRepository.findAll();

        model.addAttribute("currentUser", user.getUsername());
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("devices", devices);
        
        return "perangkat"; // ini nama file HTML tanpa .html
    }
}
