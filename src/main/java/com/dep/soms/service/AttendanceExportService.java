package com.dep.soms.service;

import com.dep.soms.dto.attendance.AttendanceRecordDto;
import com.dep.soms.dto.attendance.AttendanceReportRequest;
import com.dep.soms.dto.attendance.AttendanceSummaryDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AttendanceExportService {

    @Autowired
    private AttendanceService attendanceService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Export attendance report as Excel file
     */
    public byte[] exportAttendanceReportToExcel(AttendanceReportRequest request) throws IOException {
        List<AttendanceSummaryDto> attendanceSummaries = attendanceService.getAttendanceReport(request);

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(workbook, summarySheet, attendanceSummaries);

            // Create detailed sheet
            Sheet detailSheet = workbook.createSheet("Detailed Report");
            createDetailedSheet(workbook, detailSheet, attendanceSummaries);

            // Auto-size columns
            for (int i = 0; i < 15; i++) {
                summarySheet.autoSizeColumn(i);
                detailSheet.autoSizeColumn(i);
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Create summary sheet with attendance statistics
     */
    private void createSummarySheet(Workbook workbook, Sheet sheet, List<AttendanceSummaryDto> summaries) {
        // Create header row style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Guard ID", "Guard Name", "Total Shifts", "Present", "Absent",
                "Late", "Left Early", "Incomplete", "Total Hours Worked"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (AttendanceSummaryDto summary : summaries) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(summary.getGuardId());
            row.createCell(1).setCellValue(summary.getGuardName());
            row.createCell(2).setCellValue(summary.getTotalShifts());
            row.createCell(3).setCellValue(summary.getPresentCount());
            row.createCell(4).setCellValue(summary.getAbsentCount());
            row.createCell(5).setCellValue(summary.getLateCount());
            row.createCell(6).setCellValue(summary.getLeftEarlyCount());
            row.createCell(7).setCellValue(summary.getIncompleteCount());

            // Format hours worked as HH:MM
            Duration hoursWorked = summary.getTotalHoursWorked();
            long hours = hoursWorked.toHours();
            long minutes = hoursWorked.toMinutesPart();
            row.createCell(8).setCellValue(String.format("%d:%02d", hours, minutes));
        }
    }

    /**
     * Create detailed sheet with individual attendance records
     */
    private void createDetailedSheet(Workbook workbook, Sheet sheet, List<AttendanceSummaryDto> summaries) {
        // Create header row style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Guard Name", "Site", "Shift", "Date", "Scheduled Start", "Scheduled End",
                "Check-in Time", "Check-out Time", "Status", "Hours Worked",
                "Late By (min)", "Left Early By (min)", "Location Verified"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (AttendanceSummaryDto summary : summaries) {
            for (AttendanceRecordDto record : summary.getRecords()) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(summary.getGuardName());
                row.createCell(1).setCellValue(record.getSiteName());
                row.createCell(2).setCellValue(record.getShiftName());
                row.createCell(3).setCellValue(record.getDate().format(DATE_FORMATTER));
                row.createCell(4).setCellValue(record.getScheduledStartTime().format(TIME_FORMATTER));
                row.createCell(5).setCellValue(record.getScheduledEndTime().format(TIME_FORMATTER));

                // Check-in time
                if (record.getCheckInTime() != null) {
                    row.createCell(6).setCellValue(record.getCheckInTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    row.createCell(6).setCellValue("Not checked in");
                }

                // Check-out time
                if (record.getCheckOutTime() != null) {
                    row.createCell(7).setCellValue(record.getCheckOutTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    row.createCell(7).setCellValue("Not checked out");
                }

                row.createCell(8).setCellValue(record.getStatus());

                // Hours worked
                Duration hoursWorked = record.getHoursWorked();
                if (hoursWorked != null && !hoursWorked.isZero()) {
                    long hours = hoursWorked.toHours();
                    long minutes = hoursWorked.toMinutesPart();
                    row.createCell(9).setCellValue(String.format("%d:%02d", hours, minutes));
                } else {
                    row.createCell(9).setCellValue("0:00");
                }

                row.createCell(10).setCellValue(record.getLateByMinutes() != null ? record.getLateByMinutes() : 0);
                row.createCell(11).setCellValue(record.getLeftEarlyByMinutes() != null ? record.getLeftEarlyByMinutes() : 0);

                // Location verification
                String locationVerified = "N/A";
                if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
                    if (record.isCheckInLocationVerified() && record.isCheckOutLocationVerified()) {
                        locationVerified = "Both verified";
                    } else if (record.isCheckInLocationVerified()) {
                        locationVerified = "Check-in verified only";
                    } else if (record.isCheckOutLocationVerified()) {
                        locationVerified = "Check-out verified only";
                    } else {
                        locationVerified = "None verified";
                    }
                } else if (record.getCheckInTime() != null) {
                    locationVerified = record.isCheckInLocationVerified() ? "Check-in verified" : "Check-in not verified";
                }

                row.createCell(12).setCellValue(locationVerified);
            }
        }
    }

    /**
     * Export attendance for a single guard
     */
    public byte[] exportGuardAttendanceToExcel(Long guardId, LocalDate startDate, LocalDate endDate) throws IOException {
        AttendanceSummaryDto summary = attendanceService.getGuardAttendance(guardId, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(workbook, summarySheet, List.of(summary));

            // Create detailed sheet
            Sheet detailSheet = workbook.createSheet("Detailed Report");
            createDetailedSheet(workbook, detailSheet, List.of(summary));

            // Auto-size columns
            for (int i = 0; i < 15; i++) {
                summarySheet.autoSizeColumn(i);
                detailSheet.autoSizeColumn(i);
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
