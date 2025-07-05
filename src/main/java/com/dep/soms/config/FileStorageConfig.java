package com.dep.soms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.io.File;

@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void ensureUploadDirectoryExists() {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Bean
    public String uploadPath() {
        return uploadDir;
    }
}
