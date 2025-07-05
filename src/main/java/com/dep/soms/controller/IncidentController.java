////package com.dep.soms.controller;
////
////import com.dep.soms.dto.incident.IncidentDto;
////import com.dep.soms.dto.incident.IncidentRequest;
////import com.dep.soms.service.IncidentService;
////import jakarta.validation.Valid;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.format.annotation.DateTimeFormat;
////import org.springframework.http.ResponseEntity;
////import org.springframework.security.access.prepost.PreAuthorize;
////import org.springframework.web.bind.annotation.*;
////import org.springframework.web.multipart.MultipartFile;
////
////import java.time.LocalDate;
////import java.util.List;
////
////@CrossOrigin(origins = "*", maxAge = 3600)
////@RestController
////@RequestMapping("/api/incidents")
////public class IncidentController {
////    @Autowired
////    private IncidentService incidentService;
////
////    @GetMapping
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
////    public ResponseEntity<List<IncidentDto>> getAllIncidents() {
////        List<IncidentDto> incidents = incidentService.getAllIncidents();
////        return ResponseEntity.ok(incidents);
////    }
////
////    @GetMapping("/{id}")
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT') or hasRole('GUARD')")
////    public ResponseEntity<IncidentDto> getIncidentById(@PathVariable Long id) {
////        IncidentDto incident = incidentService.getIncidentById(id);
////        return ResponseEntity.ok(incident);
////    }
////
////    @GetMapping("/site/{siteId}")
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
////    public ResponseEntity<List<IncidentDto>> getIncidentsBySiteId(
////            @PathVariable Long siteId,
////            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
////            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
////        List<IncidentDto> incidents = incidentService.getIncidentsBySiteId(siteId, startDate, endDate);
////        return ResponseEntity.ok(incidents);
////    }
////
////    @GetMapping("/guard/{guardId}")
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
////    public ResponseEntity<List<IncidentDto>> getIncidentsByGuardId(
////            @PathVariable Long guardId,
////            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
////            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
////        List<IncidentDto> incidents = incidentService.getIncidentsByGuardId(guardId, startDate, endDate);
////        return ResponseEntity.ok(incidents);
////    }
////
////    @PostMapping
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
////    public ResponseEntity<IncidentDto> createIncident(@Valid @RequestBody IncidentRequest request) {
////        IncidentDto createdIncident = incidentService.createIncident(request);
////        return ResponseEntity.ok(createdIncident);
////    }
////
////    @PutMapping("/{id}")
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
////    public ResponseEntity<IncidentDto> updateIncident(
////            @PathVariable Long id,
////            @Valid @RequestBody IncidentRequest request) {
////        IncidentDto updatedIncident = incidentService.updateIncident(id, request);
////        return ResponseEntity.ok(updatedIncident);
////    }
////
////    @DeleteMapping("/{id}")
////    @PreAuthorize("hasRole('ADMIN')")
////    public ResponseEntity<?> deleteIncident(@PathVariable Long id) {
////        incidentService.deleteIncident(id);
////        return ResponseEntity.ok().build();
////    }
////
////    @GetMapping
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
////    public ResponseEntity<List<IncidentDto>> getAllIncidents(
////            @RequestParam(required = false) Long siteId,
////            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
////            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
////        List<IncidentDto> incidents = incidentService.getAllIncidents(siteId, startTime, endTime);
////        return ResponseEntity.ok(incidents);
////    }
////
//////    @GetMapping("/{id}")
//////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT') or hasRole('GUARD')")
//////    public ResponseEntity<IncidentDto> getIncidentById(@PathVariable Long id) {
//////        IncidentDto incident = incidentService.getIncidentById(id);
//////        return ResponseEntity.ok(incident);
//////    }
////
////    @GetMapping("/guard/{guardId}")
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @userSecurity.isCurrentGuard(#guardId)")
////    public ResponseEntity<List<IncidentDto>> getIncidentsByGuardId(@PathVariable Long guardId) {
////        List<IncidentDto> incidents = incidentService.getIncidentsByGuardId(guardId);
////        return ResponseEntity.ok(incidents);
////    }
////
////    @GetMapping("/site/{siteId}")
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
////    public ResponseEntity<List<IncidentDto>> getIncidentsBySiteId(@PathVariable Long siteId) {
////        List<IncidentDto> incidents = incidentService.getIncidentsBySiteId(siteId);
////        return ResponseEntity.ok(incidents);
////    }
////
//////    @PostMapping
//////    @PreAuthorize("hasRole('GUARD')")
//////    public ResponseEntity<IncidentDto> createIncident(@Valid @RequestBody IncidentRequest request) {
//////        IncidentDto createdIncident = incidentService.createIncident(request);
//////        return ResponseEntity.ok(createdIncident);
//////    }
//////
//////    @PutMapping("/{id}")
//////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//////    public ResponseEntity<IncidentDto> updateIncident(
//////            @PathVariable Long id,
//////            @Valid @RequestBody IncidentRequest request) {
//////        IncidentDto updatedIncident = incidentService.updateIncident(id, request);
//////        return ResponseEntity.ok(updatedIncident);
//////    }
//////
//////    @DeleteMapping("/{id}")
//////    @PreAuthorize("hasRole('ADMIN')")
//////    public ResponseEntity<?> deleteIncident(@PathVariable Long id) {
//////        incidentService.deleteIncident(id);
//////        return ResponseEntity.ok().build();
//////    }
////
////    @PostMapping("/{id}/attachments")
////    @PreAuthorize("hasRole('GUARD') or hasRole('ADMIN') or hasRole('MANAGER')")
////    public ResponseEntity<IncidentDto> addAttachment(
////            @PathVariable Long id,
////            @RequestParam("file") MultipartFile file) {
////        IncidentDto updatedIncident = incidentService.addAttachment(id, file);
////        return ResponseEntity.ok(updatedIncident);
////    }
////
////    @DeleteMapping("/{incidentId}/attachments/{attachmentId}")
////    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
////    public ResponseEntity<IncidentDto> removeAttachment(
////            @PathVariable Long incidentId,
////            @PathVariable Long attachmentId) {
////        IncidentDto updatedIncident = incidentService.removeAttachment(incidentId, attachmentId);
////        return ResponseEntity.ok(updatedIncident);
////    }
////
////}
//
//package com.dep.soms.controller;
//
//import com.dep.soms.dto.incident.IncidentDto;
//import com.dep.soms.dto.incident.IncidentRequest;
//import com.dep.soms.service.IncidentService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@CrossOrigin(origins = "*", maxAge = 3600)
//@RestController
//@RequestMapping("/api/incidents")
//public class IncidentController {
//    @Autowired
//    private IncidentService incidentService;
//
//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
//    public ResponseEntity<List<IncidentDto>> getAllIncidents(
//            @RequestParam(required = false) Long siteId,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
//        List<IncidentDto> incidents;
//        if (siteId != null || startTime != null || endTime != null) {
//            incidents = incidentService.getFilteredIncidents(siteId, startTime, endTime);
//        } else {
//            incidents = incidentService.getAllIncidents();
//        }
//        return ResponseEntity.ok(incidents);
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT') or hasRole('GUARD')")
//    public ResponseEntity<IncidentDto> getIncidentById(@PathVariable Long id) {
//        IncidentDto incident = incidentService.getIncidentById(id);
//        return ResponseEntity.ok(incident);
//    }
//
//    @GetMapping("/site/{siteId}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
//    public ResponseEntity<List<IncidentDto>> getIncidentsBySiteId(
//            @PathVariable Long siteId,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//        List<IncidentDto> incidents = incidentService.getIncidentsBySiteId(siteId, startDate, endDate);
//        return ResponseEntity.ok(incidents);
//    }
//
//    @GetMapping("/guard/{guardId}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @userSecurity.isCurrentGuard(#guardId)")
//    public ResponseEntity<List<IncidentDto>> getIncidentsByGuardId(
//            @PathVariable Long guardId,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//        List<IncidentDto> incidents = incidentService.getIncidentsByGuardId(guardId, startDate, endDate);
//        return ResponseEntity.ok(incidents);
//    }
//
//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('GUARD')")
//    public ResponseEntity<IncidentDto> createIncident(@Valid @RequestBody IncidentRequest request) {
//        IncidentDto createdIncident = incidentService.createIncident(request);
//        return ResponseEntity.ok(createdIncident);
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<IncidentDto> updateIncident(
//            @PathVariable Long id,
//            @Valid @RequestBody IncidentRequest request) {
//        IncidentDto updatedIncident = incidentService.updateIncident(id, request);
//        return ResponseEntity.ok(updatedIncident);
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> deleteIncident(@PathVariable Long id) {
//        incidentService.deleteIncident(id);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/{id}/attachments")
//    @PreAuthorize("hasRole('GUARD') or hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<IncidentDto> addAttachment(
//            @PathVariable Long id,
//            @RequestParam("file") MultipartFile file) {
//        IncidentDto updatedIncident = incidentService.addAttachment(id, file);
//        return ResponseEntity.ok(updatedIncident);
//    }
//
//    @DeleteMapping("/{incidentId}/attachments/{attachmentId}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<IncidentDto> removeAttachment(
//            @PathVariable Long incidentId,
//            @PathVariable Long attachmentId) {
//        IncidentDto updatedIncident = incidentService.removeAttachment(incidentId, attachmentId);
//        return ResponseEntity.ok(updatedIncident);
//    }
//}

package com.dep.soms.controller;

import com.dep.soms.dto.incident.IncidentDto;
import com.dep.soms.dto.incident.IncidentRequest;
import com.dep.soms.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    /**
     * Single endpoint for guards to report incidents with multiple attachments
     * Files will be served through your existing /api/uploads/{filename} endpoint
     */
    @PostMapping(value = "/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<IncidentDto> reportIncident(
            @Valid @ModelAttribute IncidentRequest request,
            Authentication authentication) {

        String guardUsername = authentication.getName();

        // Log the number of files received
        if (request.getAttachments() != null) {
            System.out.println("Received " + request.getAttachments().size() + " files");
            for (MultipartFile file : request.getAttachments()) {
                if (!file.isEmpty()) {
                    System.out.println("File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
                }
            }
        }

        IncidentDto createdIncident = incidentService.createIncidentByGuard(request, guardUsername);
        return ResponseEntity.ok(createdIncident);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<List<IncidentDto>> getAllIncidents(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<IncidentDto> incidents;
        if (siteId != null || startTime != null || endTime != null) {
            incidents = incidentService.getFilteredIncidents(siteId, startTime, endTime);
        } else {
            incidents = incidentService.getAllIncidents();
        }
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT') or hasRole('GUARD')")
    public ResponseEntity<IncidentDto> getIncidentById(@PathVariable Long id) {
        IncidentDto incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(incident);
    }

    @GetMapping("/my-incidents")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<List<IncidentDto>> getMyIncidents(Authentication authentication) {
        String guardUsername = authentication.getName();
        List<IncidentDto> incidents = incidentService.getIncidentsByGuard(guardUsername);
        return ResponseEntity.ok(incidents);
    }

    @PostMapping(value = "/{incidentId}/add-attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('GUARD') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<IncidentDto> addAttachmentsToIncident(
            @PathVariable Long incidentId,
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {

        String username = authentication.getName();
        IncidentDto updatedIncident = incidentService.addMultipleAttachments(incidentId, files, username);
        return ResponseEntity.ok(updatedIncident);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<IncidentDto> updateIncident(
            @PathVariable Long id,
            @RequestBody IncidentRequest request) {
        IncidentDto updatedIncident = incidentService.updateIncident(id, request);
        return ResponseEntity.ok(updatedIncident);
    }


    @DeleteMapping("/{incidentId}/attachments/{attachmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long incidentId,
            @PathVariable Long attachmentId) {

        incidentService.deleteAttachment(incidentId, attachmentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/guard/{id}")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<IncidentDto> updateIncidentByGuard(
            @PathVariable Long id,
            @RequestBody IncidentRequest request,
            Authentication authentication) {
        String guardUsername = authentication.getName();
        IncidentDto updatedIncident = incidentService.updateIncidentByGuard(id, request, guardUsername);
        return ResponseEntity.ok(updatedIncident);
    }

}
