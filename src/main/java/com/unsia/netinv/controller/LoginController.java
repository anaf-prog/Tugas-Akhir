package com.unsia.netinv.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.unsia.netinv.entity.Users;
import com.unsia.netinv.service.OtpService;
import com.unsia.netinv.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
    
    @Autowired
    UserService userService;

    @Autowired
    private OtpService otpService;

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
            // Simpan user sementara di session sebelum verifikasi OTP
            session.setAttribute("tempUser", users);
            
            // Kirim OTP ke email user
            otpService.sendOtp(users.getEmail());
            
            // Redirect ke halaman verifikasi OTP
            return "redirect:/verify-otp";
        } else {
            model.addAttribute("error", "Username atau password salah");
            return "login";
        }
    }

    @GetMapping("/verify-otp")
    public String showOtpVerificationPage(HttpSession session, Model model) {
        // Pastikan user sudah login sebelum verifikasi OTP
        if (session.getAttribute("tempUser") == null) {
            return "redirect:/login";
        }
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp, HttpSession session, Model model) {
        
        Users tempUser = (Users) session.getAttribute("tempUser");
        
        if (tempUser == null) {
            return "redirect:/login";
        }
        
        // Verifikasi OTP
        if (otpService.verifyOtp(tempUser.getEmail(), otp)) {
            // OTP valid, simpan user ke session dan hapus tempUser
            tempUser.setPassword(null);
            session.setAttribute("user", tempUser);
            session.removeAttribute("tempUser");
            
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Kode OTP tidak valid atau sudah kadaluarsa");
            return "verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    @ResponseBody
    public Map<String, Boolean> resendOtp(HttpSession session) {
        Map<String, Boolean> response = new HashMap<>();
        
        Users tempUser = (Users) session.getAttribute("tempUser");
        if (tempUser != null) {
            otpService.sendOtp(tempUser.getEmail());
            response.put("success", true);
        } else {
            response.put("success", false);
        }
        
        return response;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
