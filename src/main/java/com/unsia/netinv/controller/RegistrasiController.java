package com.unsia.netinv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.unsia.netinv.entity.Users;
import com.unsia.netinv.service.UserService;

@Controller
public class RegistrasiController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               @RequestParam (required = false) String role,
                               Model model) {

        if (userService.isUsernameExists(username)) {
            model.addAttribute("error", "Username sudah digunakan");
            return "register";
        }
        
        if (userService.isEmailExists(email)) {
            model.addAttribute("error", "Email sudah terdaftar");
            return "register";
        }

        Users newUser = new Users();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setRole(role != null ? role : "VIEWER");

        userService.registeruser(newUser);

        model.addAttribute("success", "Registrasi berhasil, Silahkan login");
        return "redirect:/login";

    }
}
