package com.dep.soms.service;

import com.dep.soms.model.Guard;
import com.dep.soms.model.ShiftAssignment;
import com.dep.soms.model.User;
import com.dep.soms.repository.ShiftAssignmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class AttendanceNotificationService {

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private EmailServices emailService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.name:System Administrator}")
    private String adminName;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Send notifications for guards who haven't checked in 15 minutes after shift start
     * Runs every 15 minutes
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void notifyLateGuards() {
        log.info("Running scheduled task to notify about late guards");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fifteenMinutesAgo = now.minusMinutes(15);
        LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);

        // Find assignments that started 15-30 minutes ago but haven't been checked in
        List<ShiftAssignment> lateAssignments = shiftAssignmentRepository.findByStartTimeBetweenAndCheckInTimeIsNull(
                thirtyMinutesAgo, fifteenMinutesAgo);

        log.info("Found {} late guards to notify about", lateAssignments.size());

        for (ShiftAssignment assignment : lateAssignments) {
            // Notify system admin instead of site supervisor
            sendLateGuardEmailToAdmin(assignment);

            // Notify guard
            Guard guard = assignment.getGuard();
            if (guard != null && guard.getUser() != null && guard.getUser().getEmail() != null) {
                sendLateReminderEmail(guard.getUser(), assignment);
            }
        }
    }

    /**
     * Send email to admin about late guard
     */
    private void sendLateGuardEmailToAdmin(ShiftAssignment assignment) {
        String subject = "Late Guard Alert - " + assignment.getShift().getSite().getName();
        String message = String.format(
                "Dear %s,\n\n" +
                        "This is to inform you that guard %s has not checked in for their shift.\n\n" +
                        "Shift details:\n" +
                        "- Site: %s\n" +
                        "- Shift: %s\n" +
                        "- Start time: %s\n\n" +
                        "Please take appropriate action.\n\n" +
                        "Regards,\nSecurity Operations Management System",
                adminName,
                assignment.getGuard().getUser().getFirstName() + " " + assignment.getGuard().getUser().getLastName(),
                assignment.getShift().getSite().getName(),
                assignment.getShift().getName(),
                assignment.getStartTime().format(TIME_FORMATTER)
        );

        emailService.sendEmail(adminEmail, subject, message);
    }

    /**
     * Send reminder email to guard
     */
    private void sendLateReminderEmail(User guardUser, ShiftAssignment assignment) {
        String subject = "Shift Check-in Reminder";
        String message = String.format(
                "Dear %s,\n\n" +
                        "This is a reminder that you have not checked in for your shift at %s.\n\n" +
                        "Shift details:\n" +
                        "- Site: %s\n" +
                        "- Shift: %s\n" +
                        "- Start time: %s\n\n" +
                        "Please check in as soon as possible or contact your administrator if you're having issues.\n\n" +
                        "Regards,\nSecurity Operations Management System",
                guardUser.getFirstName(),
                assignment.getShift().getSite().getName(),
                assignment.getShift().getSite().getName(),
                assignment.getShift().getName(),
                assignment.getStartTime().format(TIME_FORMATTER)
        );

        emailService.sendEmail(guardUser.getEmail(), subject, message);
    }

    /**
     * Send daily attendance summary to system admin
     * Runs every day at 23:30
     */
    @Scheduled(cron = "0 30 23 * * *")
    public void sendDailyAttendanceSummary() {
        log.info("Sending daily attendance summary");
        LocalDate today = LocalDate.now();

        // Get all sites with assignments today
        shiftAssignmentRepository.findDistinctSitesByAssignmentDate(today)
                .forEach(site -> {
                    // Get all assignments for this site today
                    List<ShiftAssignment> assignments = shiftAssignmentRepository
                            .findByShift_Site_IdAndAssignmentDate(site.getId(), today);

                    sendDailySummaryEmailToAdmin(site.getName(), assignments);
                });
    }

    /**
     * Send daily summary email to system admin
     */
    private void sendDailySummaryEmailToAdmin(String siteName, List<ShiftAssignment> assignments) {
        String subject = "Daily Attendance Summary - " + siteName + " - " + LocalDate.now();
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append(String.format(
                "Dear %s,\n\n" +
                        "Here is the attendance summary for %s on %s:\n\n",
                adminName,
                siteName,
                LocalDate.now()
        ));

        int totalAssignments = assignments.size();
        int presentCount = 0;
        int absentCount = 0;
        int lateCount = 0;
        int incompleteCount = 0;

        messageBuilder.append("Attendance Details:\n");
        messageBuilder.append("--------------------------------------------------\n");
        messageBuilder.append(String.format("%-25s %-15s %-15s %-15s\n",
                "Guard Name", "Shift", "Status", "Check-in Time"));
        messageBuilder.append("--------------------------------------------------\n");

        for (ShiftAssignment assignment : assignments) {
            String guardName = assignment.getGuard().getUser().getFirstName() + " " +
                    assignment.getGuard().getUser().getLastName();
            String shiftName = assignment.getShift().getName();
            String status = assignment.getStatus().toString();
            String checkInTime = assignment.getCheckInTime() != null ?
                    assignment.getCheckInTime().format(TIME_FORMATTER) : "Not checked in";

            messageBuilder.append(String.format("%-25s %-15s %-15s %-15s\n",
                    guardName, shiftName, status, checkInTime));

            // Count by status
            switch (assignment.getStatus()) {
                case COMPLETED:
                    presentCount++;
                    // Check if they were late
                    if (assignment.getCheckInTime() != null &&
                            assignment.getCheckInTime().isAfter(assignment.getStartTime())) {
                        lateCount++;
                    }
                    break;
                case ABSENT:
                    absentCount++;
                    break;
                case INCOMPLETE:
                    incompleteCount++;
                    break;
                default:
                    break;
            }
        }

        messageBuilder.append("--------------------------------------------------\n\n");
        messageBuilder.append("Summary:\n");
        messageBuilder.append(String.format("- Total Shifts: %d\n", totalAssignments));
        messageBuilder.append(String.format("- Present: %d\n", presentCount));
        messageBuilder.append(String.format("- Absent: %d\n", absentCount));
        messageBuilder.append(String.format("- Late: %d\n", lateCount));
        messageBuilder.append(String.format("- Incomplete: %d\n", incompleteCount));
        messageBuilder.append("\nRegards,\nSecurity Operations Management System");

        emailService.sendEmail(adminEmail, subject, messageBuilder.toString());
    }
}
