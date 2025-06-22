package com.unsia.netinv.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpService {
    private final Map<String, OtpData> otpMap = new ConcurrentHashMap<>();
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    // Waktu kadaluarsa OTP (5 menit dalam milidetik)
    private static final long OTP_EXPIRATION_TIME = 5 * 60 * 1000; 

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        long expirationTime = System.currentTimeMillis() + OTP_EXPIRATION_TIME;
        
        otpMap.put(email, new OtpData(otp, expirationTime));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Kode OTP kamu adalah: " + otp + 
                       "\nKode ini berlaku selama 5 menit.");
        message.setFrom(from);

        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String inputOtp) {
        OtpData otpData = otpMap.get(email);
        
        if (otpData == null) {
            return false; // OTP tidak ada
        }
        
        // Cek apakah OTP sudah kadaluarsa
        if (System.currentTimeMillis() > otpData.getExpirationTime()) {
            otpMap.remove(email); // Hapus OTP yang sudah kadaluarsa
            return false;
        }
        
        // Verifikasi OTP
        if (otpData.getOtp().equals(inputOtp)) {
            otpMap.remove(email); // hapus setelah dipakai
            return true;
        }
        
        return false;
    }

    // Kelas pembantu untuk menyimpan OTP dan waktu kadaluarsa
    private static class OtpData {
        private final String otp;
        private final long expirationTime;

        public OtpData(String otp, long expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpirationTime() {
            return expirationTime;
        }
    }
}
