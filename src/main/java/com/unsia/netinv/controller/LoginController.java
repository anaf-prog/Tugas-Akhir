package com.unsia.netinv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.unsia.netinv.entity.Users;
import com.unsia.netinv.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
    
    @Autowired
    UserService userService;

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("success", "Registrasi berhasil, Silahkan login");
        
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Users users = userService.authenticate(username, password);

        if (users != null) {
            users.setPassword(null);
            session.setAttribute("user", users);
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Username atau password salah");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
