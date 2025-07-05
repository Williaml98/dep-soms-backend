package com.dep.soms.controller;

import com.dep.soms.dto.report.*;
import com.dep.soms.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/attendance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<AttendanceReportDto> getAttendanceReport(
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long guardId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AttendanceReportDto report = reportService.generateAttendanceReport(siteId, guardId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/incidents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<IncidentReportDto> getIncidentReport(
            @RequestParam(required = false) Long siteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        IncidentReportDto report = reportService.generateIncidentReport(siteId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/site-coverage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<SiteCoverageReportDto> getSiteCoverageReport(
            @RequestParam(required = false) Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        SiteCoverageReportDto report = reportService.generateSiteCoverageReport(clientId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/guard-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<GuardPerformanceReportDto> getGuardPerformanceReport(
            @RequestParam(required = false) Long guardId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        GuardPerformanceReportDto report = reportService.generateGuardPerformanceReport(guardId, startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
