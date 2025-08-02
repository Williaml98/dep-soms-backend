//package com.dep.soms.service;
//
//import com.dep.soms.dto.patrol.*;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@Service
//public class PatrolExportService {
//
//    @Autowired
//    private PatrolAttendanceService patrolAttendanceService;
//
//    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
//
//    public byte[] exportPatrolAttendanceReportToExcel(PatrolAttendanceReportRequest request) throws IOException {
//        List<PatrolAttendanceSummaryDto> summaries = patrolAttendanceService.getPatrolAttendanceReport(request);
//
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Patrol Attendance");
//
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            createHeaderCells(headerRow, workbook);
//
//            // Fill data rows
//            int rowNum = 1;
//            for (PatrolAttendanceSummaryDto summary : summaries) {
//                for (PatrolAttendanceRecordDto record : summary.getRecords()) {
//                    Row row = sheet.createRow(rowNum++);
//                    fillRecordData(row, record, workbook);
//                }
//            }
//
//            // Auto-size columns
//            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
//                sheet.autoSizeColumn(i);
//            }
//
//            // Write to byte array
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    public byte[] exportSupervisorPatrolAttendanceToExcel(Long supervisorId, LocalDate startDate, LocalDate endDate) throws IOException {
//        PatrolAttendanceSummaryDto summary = patrolAttendanceService.getSupervisorPatrolAttendance(supervisorId, startDate, endDate);
//
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Supervisor Patrol Attendance");
//
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            createHeaderCells(headerRow, workbook);
//
//            // Fill data rows
//            int rowNum = 1;
//            for (PatrolAttendanceRecordDto record : summary.getRecords()) {
//                Row row = sheet.createRow(rowNum++);
//                fillRecordData(row, record, workbook);
//            }
//
//            // Add summary statistics
//            Row summaryRow1 = sheet.createRow(rowNum++);
//            summaryRow1.createCell(0).setCellValue("Total Patrols");
//            summaryRow1.createCell(1).setCellValue(summary.getTotalPatrols());
//
//            Row summaryRow2 = sheet.createRow(rowNum++);
//            summaryRow2.createCell(0).setCellValue("Completed Patrols");
//            summaryRow2.createCell(1).setCellValue(summary.getCompletedCount());
//
//            Row summaryRow3 = sheet.createRow(rowNum++);
//            summaryRow3.createCell(0).setCellValue("Average Completion %");
//            summaryRow3.createCell(1).setCellValue(summary.getAvgCompletionPercentage());
//
//            // Auto-size columns
//            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
//                sheet.autoSizeColumn(i);
//            }
//
//            // Write to byte array
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    private void createHeaderCells(Row headerRow, Workbook workbook) {
//        CellStyle headerStyle = createHeaderStyle(workbook);
//
//        String[] headers = {
//                "Patrol ID", "Patrol Name", "Supervisor", "Site", "Date",
//                "Scheduled Start", "Scheduled End", "Actual Start", "Actual End",
//                "Total Checkpoints", "Completed Checkpoints", "Completion %", "Status",
//                "Checkpoint Name", "Expected Time", "Actual Time", "Location Verified",
//                "Distance (m)", "Notes"
//        };
//
//        for (int i = 0; i < headers.length; i++) {
//            Cell cell = headerRow.createCell(i);
//            cell.setCellValue(headers[i]);
//            cell.setCellStyle(headerStyle);
//        }
//    }
//
//    private void fillRecordData(Row row, PatrolAttendanceRecordDto record, Workbook workbook) {
//        CellStyle dateStyle = createDateStyle(workbook);
//        CellStyle timeStyle = createTimeStyle(workbook);
//
//        int colNum = 0;
//
//        // Basic patrol info
//        row.createCell(colNum++).setCellValue(record.getPatrolId());
//        row.createCell(colNum++).setCellValue(record.getPatrolName());
//        row.createCell(colNum++).setCellValue(record.getSupervisorName());
//        row.createCell(colNum++).setCellValue(record.getSiteName());
//
//        Cell dateCell = row.createCell(colNum++);
//        dateCell.setCellValue(record.getDate().format(DATE_FORMATTER));
//        dateCell.setCellStyle(dateStyle);
//
//        Cell scheduledStartCell = row.createCell(colNum++);
//        scheduledStartCell.setCellValue(record.getScheduledStartTime().format(TIME_FORMATTER));
//        scheduledStartCell.setCellStyle(timeStyle);
//
//        Cell scheduledEndCell = row.createCell(colNum++);
//        scheduledEndCell.setCellValue(record.getScheduledEndTime().format(TIME_FORMATTER));
//        scheduledEndCell.setCellStyle(timeStyle);
//
//        Cell actualStartCell = row.createCell(colNum++);
//        if (record.getActualStartTime() != null) {
//            actualStartCell.setCellValue(record.getActualStartTime().format(TIME_FORMATTER));
//            actualStartCell.setCellStyle(timeStyle);
//        }
//
//        Cell actualEndCell = row.createCell(colNum++);
//        if (record.getActualEndTime() != null) {
//            actualEndCell.setCellValue(record.getActualEndTime().format(TIME_FORMATTER));
//            actualEndCell.setCellStyle(timeStyle);
//        }
//
//        row.createCell(colNum++).setCellValue(record.getTotalCheckpoints());
//        row.createCell(colNum++).setCellValue(record.getCompletedCheckpoints());
//        row.createCell(colNum++).setCellValue(record.getCompletionPercentage());
//        row.createCell(colNum++).setCellValue(record.getStatus());
//
//        // Checkpoint details (one row per checkpoint)
//        for (PatrolCheckpointDto checkpoint : record.getCheckpoints()) {
//            if (colNum > 12) { // If we're not on the first checkpoint row
//                row = sheet.createRow(row.getRowNum() + 1);
//                colNum = 13; // Start at checkpoint name column
//            }
//
//            row.createCell(colNum++).setCellValue(checkpoint.getPatrolPointName());
//
//            Cell expectedTimeCell = row.createCell(colNum++);
//            if (checkpoint.getExpectedTime() != null) {
//                expectedTimeCell.setCellValue(checkpoint.getExpectedTime().format(TIME_FORMATTER));
//                expectedTimeCell.setCellStyle(timeStyle);
//            }
//
//            Cell actualTimeCell = row.createCell(colNum++);
//            if (checkpoint.getActualTime() != null) {
//                actualTimeCell.setCellValue(checkpoint.getActualTime().format(TIME_FORMATTER));
//                actualTimeCell.setCellStyle(timeStyle);
//            }
//
//            row.createCell(colNum++).setCellValue(checkpoint.isLocationVerified() ? "Yes" : "No");
//            row.createCell(colNum++).setCellValue(checkpoint.getDistanceFromExpected());
//            row.createCell(colNum++).setCellValue(checkpoint.getNotes());
//        }
//    }
//
//    private CellStyle createHeaderStyle(Workbook workbook) {
//        CellStyle style = workbook.createCellStyle();
//        Font font = workbook.createFont();
//        font.setBold(true);
//        style.setFont(font);
//        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        style.setBorderBottom(BorderStyle.THIN);
//        style.setBorderTop(BorderStyle.THIN);
//        style.setBorderLeft(BorderStyle.THIN);
//        style.setBorderRight(BorderStyle.THIN);
//        return style;
//    }
//
//    private CellStyle createDateStyle(Workbook workbook) {
//        CellStyle style = workbook.createCellStyle();
//        CreationHelper createHelper = workbook.getCreationHelper();
//        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
//        return style;
//    }
//
//    private CellStyle createTimeStyle(Workbook workbook) {
//        CellStyle style = workbook.createCellStyle();
//        CreationHelper createHelper = workbook.getCreationHelper();
//        style.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));
//        return style;
//    }
//}

package com.dep.soms.service;

import com.dep.soms.dto.patrol.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PatrolExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private PatrolAttendanceService patrolAttendanceService;

    public byte[] exportPatrolAttendanceReportToExcel(PatrolAttendanceReportRequest request) throws IOException {
        List<PatrolAttendanceSummaryDto> summaries = patrolAttendanceService.getPatrolAttendanceReport(request);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Patrol Attendance");

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow, workbook);

            // Fill data rows
            int rowNum = 1;
            for (PatrolAttendanceSummaryDto summary : summaries) {
                for (PatrolAttendanceRecordDto record : summary.getRecords()) {
                    Row row = sheet.createRow(rowNum++);
                    fillRecordData(workbook, sheet, row, record);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportSupervisorPatrolAttendanceToExcel(Long supervisorId, LocalDate startDate, LocalDate endDate) throws IOException {
        PatrolAttendanceSummaryDto summary = patrolAttendanceService.getSupervisorPatrolAttendance(supervisorId, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Supervisor Patrol Attendance");

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCells(headerRow, workbook);

            // Fill data rows
            int rowNum = 1;
            for (PatrolAttendanceRecordDto record : summary.getRecords()) {
                Row row = sheet.createRow(rowNum++);
                fillRecordData(workbook, sheet, row, record);
            }

            // Add summary statistics
            Row summaryRow1 = sheet.createRow(rowNum++);
            summaryRow1.createCell(0).setCellValue("Total Patrols");
            summaryRow1.createCell(1).setCellValue(summary.getTotalPatrols());

            Row summaryRow2 = sheet.createRow(rowNum++);
            summaryRow2.createCell(0).setCellValue("Completed Patrols");
            summaryRow2.createCell(1).setCellValue(summary.getCompletedCount());

            Row summaryRow3 = sheet.createRow(rowNum++);
            summaryRow3.createCell(0).setCellValue("Average Completion %");
            summaryRow3.createCell(1).setCellValue(summary.getAvgCompletionPercentage());

            // Auto-size columns
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createHeaderCells(Row headerRow, Workbook workbook) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        String[] headers = {
                "Patrol ID", "Patrol Name", "Supervisor", "Site", "Date",
                "Scheduled Start", "Scheduled End", "Actual Start", "Actual End",
                "Total Checkpoints", "Completed Checkpoints", "Completion %", "Status",
                "Checkpoint Name", "Expected Time", "Actual Time", "Location Verified",
                "Distance (m)", "Notes"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillRecordData(Workbook workbook, Sheet sheet, Row row, PatrolAttendanceRecordDto record) {
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle timeStyle = createTimeStyle(workbook);

        int colNum = 0;

        // Basic patrol info
        row.createCell(colNum++).setCellValue(record.getPatrolId());
        row.createCell(colNum++).setCellValue(record.getPatrolName());
        row.createCell(colNum++).setCellValue(record.getSupervisorName());
        row.createCell(colNum++).setCellValue(record.getSiteName());

        Cell dateCell = row.createCell(colNum++);
        dateCell.setCellValue(record.getDate().format(DATE_FORMATTER));
        dateCell.setCellStyle(dateStyle);

        Cell scheduledStartCell = row.createCell(colNum++);
        scheduledStartCell.setCellValue(record.getScheduledStartTime().format(TIME_FORMATTER));
        scheduledStartCell.setCellStyle(timeStyle);

        Cell scheduledEndCell = row.createCell(colNum++);
        scheduledEndCell.setCellValue(record.getScheduledEndTime().format(TIME_FORMATTER));
        scheduledEndCell.setCellStyle(timeStyle);

        Cell actualStartCell = row.createCell(colNum++);
        if (record.getActualStartTime() != null) {
            actualStartCell.setCellValue(record.getActualStartTime().toLocalTime().format(TIME_FORMATTER));
            actualStartCell.setCellStyle(timeStyle);
        }

        Cell actualEndCell = row.createCell(colNum++);
        if (record.getActualEndTime() != null) {
            actualEndCell.setCellValue(record.getActualEndTime().toLocalTime().format(TIME_FORMATTER));
            actualEndCell.setCellStyle(timeStyle);
        }

        row.createCell(colNum++).setCellValue(record.getTotalCheckpoints());
        row.createCell(colNum++).setCellValue(record.getCompletedCheckpoints());
        row.createCell(colNum++).setCellValue(record.getCompletionPercentage());
        row.createCell(colNum++).setCellValue(record.getStatus());

        // Checkpoint details (one row per checkpoint)
        if (record.getCheckpoints() != null && !record.getCheckpoints().isEmpty()) {
            for (PatrolCheckpointDto checkpoint : record.getCheckpoints()) {
                if (colNum > 12) { // If we're not on the first checkpoint row
                    row = sheet.createRow(row.getRowNum() + 1);
                    colNum = 13; // Start at checkpoint name column
                }

                row.createCell(colNum++).setCellValue(checkpoint.getPatrolPointName());

                Cell expectedTimeCell = row.createCell(colNum++);
                if (checkpoint.getExpectedTime() != null) {
                    expectedTimeCell.setCellValue(checkpoint.getExpectedTime().format(TIME_FORMATTER));
                    expectedTimeCell.setCellStyle(timeStyle);
                }

                Cell actualTimeCell = row.createCell(colNum++);
                if (checkpoint.getActualTime() != null) {
                    actualTimeCell.setCellValue(checkpoint.getActualTime().toLocalTime().format(TIME_FORMATTER));
                    actualTimeCell.setCellStyle(timeStyle);
                }

                row.createCell(colNum++).setCellValue(checkpoint.isLocationVerified() ? "Yes" : "No");
                row.createCell(colNum++).setCellValue(checkpoint.getDistanceFromExpected());
                row.createCell(colNum++).setCellValue(checkpoint.getNotes());
            }
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
        return style;
    }

    private CellStyle createTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm"));
        return style;
    }
}