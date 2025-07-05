package com.dep.soms.controller;

import com.dep.soms.dto.attendance.AttendanceReportRequest;
import com.dep.soms.dto.attendance.AttendanceSummaryDto;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Guard;
import com.dep.soms.model.User;
import com.dep.soms.repository.GuardRepository;
import com.dep.soms.repository.UserRepository;
import com.dep.soms.service.AttendanceExportService;
import com.dep.soms.service.AttendanceSchedulerService;
import com.dep.soms.service.AttendanceService;
import com.dep.soms.service.UserService;
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
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceSchedulerService attendanceSchedulerService;
    @Autowired
    private AttendanceExportService attendanceExportService;

    @Autowired
    private UserService userService;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get attendance for the currently logged-in user (guard)
     */
    @GetMapping("/my-attendance")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<AttendanceSummaryDto> getMyAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Get current user using your existing pattern
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get the guard for this user
        Guard guard = guardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found for user: " + user.getId()));

        AttendanceSummaryDto summary = attendanceService.getGuardAttendance(guard.getId(), startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get attendance for a specific guard (admin and client access)
     */
    @GetMapping("/guard/{guardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<AttendanceSummaryDto> getGuardAttendance(
            @PathVariable Long guardId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        AttendanceSummaryDto summary = attendanceService.getGuardAttendance(guardId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get attendance for all guards at a specific site (admin and client access)
     */
    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<AttendanceSummaryDto>> getSiteAttendance(
            @PathVariable Long siteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<AttendanceSummaryDto> summaries = attendanceService.getSiteAttendance(siteId, startDate, endDate);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Generate custom attendance report with filters (admin and client access)
     */
    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<AttendanceSummaryDto>> generateAttendanceReport(
            @RequestBody AttendanceReportRequest request) {

        List<AttendanceSummaryDto> report = attendanceService.getAttendanceReport(request);
        return ResponseEntity.ok(report);
    }

    /**
     * Client-specific endpoint to get attendance for all guards at their sites
     */
    @GetMapping("/client/my-sites")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<AttendanceSummaryDto>> getClientSitesAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Get current user using your existing pattern
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // This assumes you have a method to get sites associated with a client
        List<Long> clientSiteIds = userService.getSiteIdsForClient(user.getId());

        AttendanceReportRequest request = new AttendanceReportRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setSiteIds(clientSiteIds);

        List<AttendanceSummaryDto> report = attendanceService.getAttendanceReport(request);
        return ResponseEntity.ok(report);
    }

    /**
     * Export attendance report as Excel
     */
    @PostMapping("/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<byte[]> exportAttendanceReportToExcel(
            @RequestBody AttendanceReportRequest request) throws IOException {

        byte[] excelBytes = attendanceExportService.exportAttendanceReportToExcel(request);

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=attendance_report.xlsx")
                .body(excelBytes);
    }

    /**
     * Export guard attendance as Excel
     */
    @GetMapping("/export/guard/{guardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<byte[]> exportGuardAttendanceToExcel(
            @PathVariable Long guardId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {

        byte[] excelBytes = attendanceExportService.exportGuardAttendanceToExcel(guardId, startDate, endDate);

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=guard_attendance_report.xlsx")
                .body(excelBytes);
    }

    /**
     * Export my attendance as Excel (for guards)
     */
    @GetMapping("/export/my-attendance")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<byte[]> exportMyAttendanceToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) throws IOException {

        // Get current user using your existing pattern
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get the guard for this user
        Guard guard = guardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found for user: " + user.getId()));

        byte[] excelBytes = attendanceExportService.exportGuardAttendanceToExcel(guard.getId(), startDate, endDate);

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=my_attendance_report.xlsx")
                .body(excelBytes);
    }

    @PostMapping("/manual")
    public ResponseEntity<String> manuallyTriggerAttendance(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        attendanceSchedulerService.manuallyMarkAttendance(parsedDate);
        return ResponseEntity.ok("Attendance processed for: " + date);
    }

    @PostMapping("/manual/month")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> manuallyMarkMonth() {
        attendanceSchedulerService.manuallyMarkAttendanceForLastMonth();
        return ResponseEntity.ok("Attendance marked for the entire previous month.");
    }


}
