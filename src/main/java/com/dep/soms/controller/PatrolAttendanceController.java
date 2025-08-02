package com.dep.soms.controller;

import com.dep.soms.dto.patrol.*;
import com.dep.soms.model.User;
import com.dep.soms.service.PatrolAttendanceService;
import com.dep.soms.service.PatrolExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patrol-attendance")
public class PatrolAttendanceController {

    @Autowired
    private PatrolAttendanceService patrolAttendanceService;

    @Autowired
    private PatrolExportService patrolExportService;

    /**
     * Get patrol attendance for the currently logged-in supervisor
     */
    @GetMapping("/my-attendance")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<PatrolAttendanceSummaryDto> getMyPatrolAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long userId = getCurrentUserId();
        PatrolAttendanceSummaryDto summary = patrolAttendanceService.getMyPatrolAttendance(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get patrol attendance for a specific supervisor (admin and manager access)
     */
    @GetMapping("/supervisor/{supervisorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PatrolAttendanceSummaryDto> getSupervisorPatrolAttendance(
            @PathVariable Long supervisorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        PatrolAttendanceSummaryDto summary = patrolAttendanceService.getSupervisorPatrolAttendance(supervisorId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get patrol attendance for all supervisors at a specific site (admin and manager access)
     */
    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<PatrolAttendanceSummaryDto>> getSitePatrolAttendance(
            @PathVariable Long siteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<PatrolAttendanceSummaryDto> summaries = patrolAttendanceService.getSitePatrolAttendance(siteId, startDate, endDate);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Generate custom patrol attendance report with filters (admin and manager access)
     */
    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<PatrolAttendanceSummaryDto>> generatePatrolAttendanceReport(
            @RequestBody PatrolAttendanceReportRequest request) {

        List<PatrolAttendanceSummaryDto> report = patrolAttendanceService.getPatrolAttendanceReport(request);
        return ResponseEntity.ok(report);
    }

    /**
     * Export patrol attendance report as Excel
     */
    @PostMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportPatrolAttendanceReportToExcel(
            @RequestBody PatrolAttendanceReportRequest request) throws IOException {

        byte[] excelBytes = patrolExportService.exportPatrolAttendanceReportToExcel(request);

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=patrol_attendance_report.xlsx")
                .body(excelBytes);
    }

    /**
     * Export supervisor patrol attendance as Excel
     */
    @GetMapping("/export/supervisor/{supervisorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> exportSupervisorPatrolAttendanceToExcel(
            @PathVariable Long supervisorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {

        byte[] excelBytes = patrolExportService.exportSupervisorPatrolAttendanceToExcel(supervisorId, startDate, endDate);

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=supervisor_patrol_attendance.xlsx")
                .body(excelBytes);
    }

    /**
     * Export my patrol attendance as Excel (for supervisors)
     */
    @GetMapping("/export/my-attendance")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<byte[]> exportMyPatrolAttendanceToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {

        Long userId = getCurrentUserId();
        byte[] excelBytes = patrolExportService.exportSupervisorPatrolAttendanceToExcel(userId, startDate, endDate);

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=my_patrol_attendance.xlsx")
                .body(excelBytes);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((User) authentication.getPrincipal()).getId();
    }

//
//    @PostMapping("/export/excel")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//    public ResponseEntity<byte[]> exportPatrolAttendanceReportToExcel(
//            @RequestBody PatrolAttendanceReportRequest request) throws IOException {
//        byte[] excelBytes = patrolExportService.exportPatrolAttendanceReportToExcel(request);
//        return createExcelResponse(excelBytes, "patrol_attendance_report.xlsx");
//    }
//
//    @GetMapping("/export/supervisor/{supervisorId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
//    public ResponseEntity<byte[]> exportSupervisorPatrolAttendanceToExcel(
//            @PathVariable Long supervisorId,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
//        byte[] excelBytes = patrolExportService.exportSupervisorPatrolAttendanceToExcel(supervisorId, startDate, endDate);
//        return createExcelResponse(excelBytes, "supervisor_patrol_attendance.xlsx");
//    }
//
//    @GetMapping("/export/my-attendance")
//    @PreAuthorize("hasRole('SUPERVISOR')")
//    public ResponseEntity<byte[]> exportMyPatrolAttendanceToExcel(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {
//        Long userId = getCurrentUserId();
//        byte[] excelBytes = patrolExportService.exportSupervisorPatrolAttendanceToExcel(userId, startDate, endDate);
//        return createExcelResponse(excelBytes, "my_patrol_attendance.xlsx");
//    }

    private ResponseEntity<byte[]> createExcelResponse(byte[] excelBytes, String filename) {
        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(excelBytes);
    }
}