//package com.dep.soms.controller;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@CrossOrigin(origins = "*", maxAge = 3600)
//@RestController
//@RequestMapping("/api/uploads")
//public class FileController {
//
//    @Value("${app.upload.dir:uploads}")
//    private String uploadDir;
//
//    @GetMapping("/{filename:.+}")
//    @PreAuthorize("hasRole('USER') or hasRole('GUARD') or hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//        try {
//            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
//            Resource resource = new UrlResource(filePath.toUri());
//
//            if (resource.exists() && resource.isReadable()) {
//                // Determine content type
//                String contentType = null;
//                try {
//                    contentType = Files.probeContentType(filePath);
//                } catch (IOException ex) {
//                    // Fallback to default content type
//                    contentType = "application/octet-stream";
//                }
//
//                // If content type is null, set default
//                if (contentType == null) {
//                    contentType = "application/octet-stream";
//                }
//
//                return ResponseEntity.ok()
//                        .contentType(MediaType.parseMediaType(contentType))
//                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
//                        .body(resource);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (MalformedURLException ex) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//}


package com.dep.soms.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/uploads")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/{filename:.+}")
    @PreAuthorize("hasRole('USER') or hasRole('GUARD') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = null;
                try {
                    contentType = Files.probeContentType(filePath);
                } catch (IOException ex) {
                    // Fallback to default content type
                    contentType = "application/octet-stream";
                }

                // If content type is null, set default
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.err.println("File not found or not readable: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            System.err.println("Malformed URL for filename: " + filename);
            ex.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            System.err.println("Error serving file: " + filename);
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
