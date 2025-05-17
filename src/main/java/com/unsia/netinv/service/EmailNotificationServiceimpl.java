package com.unsia.netinv.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.unsia.netinv.entity.Device;
import com.unsia.netinv.entity.Users;
import com.unsia.netinv.repository.UserRepository;

import jakarta.persistence.EntityManager;

@Service
public class EmailNotificationServiceimpl implements EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationServiceimpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void sendDeviceDownNotification(Device device, String additionalMessage) {
         try {
            
            Device manageDevice = entityManager.merge(device);

            String locationName = manageDevice.getLocation() != null ? manageDevice.getLocation().getLocationName() : "Lokasi tidak tersedia";

            // 1. Ambil semua email viewer dari database
            List<Users> viewers = userRepository.findByRole("VIEWER");
            
            // 2. Ekstrak alamat email saja
            String[] toEmails = viewers.stream()
                .map(Users::getEmail)
                .toArray(String[]::new);
            
            // 3. Jika tidak ada viewer, log dan return
            if (toEmails.length == 0) {
                logger.warn("Tidak ada VIEWER terdaftar untuk mengirim notifikasi");
                return;
            }

            // 4. Buat dan kirim email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmails); // Sekarang mengirim ke semua viewer
            message.setSubject("[Alert] Perangkat " + manageDevice.getDeviceName() + " Mati");
            message.setText(String.format(
                "Perangkat %s (%s) dengan IP %s terdeteksi mati/offline.\n\n" +
                "Lokasi: %s\n" +
                "Waktu: %s\n" +
                "Pesan: %s\n\n" +
                "Silakan segera lakukan pengecekan.",
                manageDevice.getDeviceName(),
                manageDevice.getDeviceType(),
                manageDevice.getIpAddress(),
                locationName,
                new Date(),
                additionalMessage
            ));

            mailSender.send(message);
            logger.info("Notifikasi email terkirim ke {} VIEWER untuk perangkat {} yang mati", 
                toEmails.length, manageDevice.getDeviceName());
            
        } catch (Exception e) {
            logger.error("Gagal mengirim notifikasi email untuk perangkat {}: {}", 
                device.getDeviceName(), e.getMessage());
        }
    }
    
}
