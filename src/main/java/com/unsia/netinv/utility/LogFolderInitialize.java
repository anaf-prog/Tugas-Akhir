package com.unsia.netinv.utility;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class LogFolderInitialize {
    private static final String LOG_DIR = "./logs";

    private static final Logger logger = LoggerFactory.getLogger(LogFolderInitialize.class);

    @PostConstruct
    public void createLogDirectory() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                logger.info("Folder logs berhasil dibuat: " + LOG_DIR);
            } else {
                logger.info("Gagal membuat folder logs: " + LOG_DIR);
            }
        } else {
            logger.info("Folder logs sudah ada.");
        }
    }
    
}
