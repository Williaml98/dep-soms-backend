package com.dep.soms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File name is null");
        }

        String fileExtension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileExtension = originalFilename.substring(lastDotIndex);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String newFilename = "incident_" + timestamp + "_" + uniqueId + fileExtension;

        // Save the file
        Path targetLocation = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return newFilename; // Return just the filename, not the full path
    }

    public void deleteFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        Files.deleteIfExists(filePath);
    }

    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }
}
