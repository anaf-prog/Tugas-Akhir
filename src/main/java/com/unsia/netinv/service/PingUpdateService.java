package com.unsia.netinv.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PingUpdateService {
    private static final Logger logger = LoggerFactory.getLogger(PingUpdateService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public PingUpdateService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendPingUpdate(Long deviceId, Date pingTime, String status) {
    try {
        Map<String, Object> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        payload.put("pingTime", pingTime.getTime());
        payload.put("status", status);
        
        messagingTemplate.convertAndSend("/topic/ping-updates", payload);
    } catch (Exception e) {
        
        logger.error("ERROR sending real-time update: " + e.getMessage(), e);
    }
}
    
}
