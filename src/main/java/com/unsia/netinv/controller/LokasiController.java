package com.unsia.netinv.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.unsia.netinv.entity.Users;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/lokasi")
public class LokasiController {
    
    @GetMapping
    public String showLokasiPage(Model model, HttpSession session) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", user.getUsername());
        model.addAttribute("userRole", user.getRole());

        return "lokasi"; // ini nama file HTML tanpa .html
    }
}
