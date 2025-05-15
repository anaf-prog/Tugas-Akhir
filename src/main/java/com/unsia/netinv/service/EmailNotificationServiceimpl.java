package com.unsia.netinv.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.unsia.netinv.entity.Device;

@Service
public class EmailNotificationServiceimpl implements EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationServiceimpl.class);

    @Autowired
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendDeviceDownNotification(Device device, String additionalMessage) {
         try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("izzatannafs6@gmail.com");
            message.setSubject("[Alert] Perangkat " + device.getDeviceName() + " Mati");
            message.setText(String.format(
                "Perangkat %s (%s) dengan IP %s terdeteksi mati/offline.\n\n" +
                "Waktu: %s\n" +
                "Pesan: %s\n\n" +
                "Silakan segera lakukan pengecekan.",
                device.getDeviceName(),
                device.getDeviceType(),
                device.getIpAddress(),
                new Date(),
                additionalMessage
            ));

            mailSender.send(message);
            logger.info("Notifikasi email terkirim untuk perangkat {} yang mati", device.getDeviceName());
        } catch (Exception e) {
            logger.error("Gagal mengirim notifikasi email untuk perangkat {}: {}", device.getDeviceName(), e.getMessage());
        }
    }
    
}
