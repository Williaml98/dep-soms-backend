//package com.dep.soms.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender emailSender;
//
//    public void sendAccountCreationEmail(String to, String username, String password, String client) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("Your SOMS Account Has Been Created");
//        message.setText("Hello,\n\n" +
//                "Your account has been created in the Security Operations Management System.\n\n" +
//                "Username: " + username + "\n" +
//                "Temporary Password: " + password + "\n\n" +
//                "Please log in and change your password as soon as possible.\n\n" +
//                "Regards,\nSOMS Admin Team");
//
//        emailSender.send(message);
//    }
//
//    public void sendShiftAssignmentEmail(String to, String guardName, String shiftName,
//                                         String siteName, LocalDate date, LocalTime startTime, LocalTime endTime) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("New Shift Assignment - " + shiftName);
//        message.setText("Hello " + guardName + ",\n\n" +
//                "You have been assigned to a new shift in the Security Operations Management System.\n\n" +
//                "Shift Details:\n" +
//                "Shift Name: " + shiftName + "\n" +
//                "Site: " + siteName + "\n" +
//                "Date: " + date + "\n" +
//                "Start Time: " + startTime + "\n" +
//                "End Time: " + endTime + "\n\n" +
//                "Please ensure you arrive on time and bring all necessary equipment.\n" +
//                "Contact your supervisor if you have any questions.\n\n" +
//                "Regards,\nSOMS Admin Team");
//        emailSender.send(message);
//    }
//
//    public void sendBulkAssignmentSummaryEmail(String to, String adminName, String shiftName,
//                                               String dateRange, int totalCreated, int warnings, int errors) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("Bulk Assignment Summary - " + shiftName);
//        message.setText("Hello " + adminName + ",\n\n" +
//                "Your bulk shift assignment operation has been completed.\n\n" +
//                "Summary:\n" +
//                "Shift: " + shiftName + "\n" +
//                "Date Range: " + dateRange + "\n" +
//                "Total Assignments Created: " + totalCreated + "\n" +
//                "Warnings: " + warnings + "\n" +
//                "Errors: " + errors + "\n\n" +
//                (warnings > 0 || errors > 0 ?
//                        "Please review the assignment details for any issues.\n\n" :
//                        "All assignments were created successfully.\n\n") +
//                "Regards,\nSOMS Admin Team");
//        emailSender.send(message);
//    }
//
//
//}


package com.dep.soms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    // Basic email sending method
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    // Registration Workflow Emails
    public void sendRegistrationAcknowledgement(String email, String firstName, String lastName, Long applicationId) {
        String subject = "Application Received - Security Position";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "Thank you for submitting your application for the security position.\n\n" +
                        "Application Details:\n" +
                        "Application ID: %d\n" +
                        "Status: Under Review\n\n" +
                        "We will review your application and contact you within 5-7 business days.\n\n" +
                        "You can check your application status by contacting HR at hr@security.com.\n\n" +
                        "Regards,\nSecurity Recruitment Team",
                firstName, lastName, applicationId
        );
        sendSimpleEmail(email, subject, text);
    }

    public void sendApplicationApproval(String email, String firstName, String lastName) {
        String subject = "Application Approved - Next Steps";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "Congratulations! Your application has been approved.\n\n" +
                        "Next Steps:\n" +
                        "1. Background verification process will begin\n" +
                        "2. You'll receive a medical examination schedule\n" +
                        "3. Prepare your original documents for verification\n\n" +
                        "We will contact you within 3 business days with more details.\n\n" +
                        "Regards,\nSecurity Recruitment Team",
                firstName, lastName
        );
        sendSimpleEmail(email, subject, text);
    }

    public void sendTrainingInvitation(String email, String firstName, String lastName,
                                       LocalDate startDate, LocalTime time, String location) {
        String subject = "Security Training Program Invitation";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "You have been scheduled for security training:\n\n" +
                        "Training Details:\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Location: %s\n" +
                        "Duration: 2 weeks\n\n" +
                        "Required Items:\n" +
                        "- Original ID documents\n" +
                        "- Notebook and pen\n" +
                        "- Black pants and white shirt (first day)\n\n" +
                        "Please confirm your attendance by replying to this email.\n\n" +
                        "Regards,\nSecurity Training Academy",
                firstName, lastName, startDate, time, location
        );
        sendSimpleEmail(email, subject, text);
    }

    public void sendTrainingCompletion(String email, String firstName, String lastName) {
        String subject = "Training Program Completed";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "Congratulations on successfully completing your security training!\n\n" +
                        "Next Steps:\n" +
                        "1. Your certification will be processed within 5 business days\n" +
                        "2. You'll receive your guard license number via email\n" +
                        "3. Assignment details will follow\n\n" +
                        "Regards,\nSecurity Training Academy",
                firstName, lastName
        );
        sendSimpleEmail(email, subject, text);
    }

    public void sendGuardApproval(String email, String firstName, String lastName,
                                  String guardId, LocalDate startDate) {
        String subject = "Welcome to Our Security Team!";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "We are pleased to welcome you to our security team!\n\n" +
                        "Your Guard Details:\n" +
                        "Guard ID: %s\n" +
                        "Start Date: %s\n\n" +
                        "Next Steps:\n" +
                        "1. Visit our office for uniform fitting\n" +
                        "2. Complete final paperwork\n" +
                        "3. Attend orientation session\n\n" +
                        "Regards,\nSecurity Operations Manager",
                firstName, lastName, guardId, startDate
        );
        sendSimpleEmail(email, subject, text);
    }

    public void sendApplicationRejection(String email, String firstName, String lastName, String reason) {
        String subject = "Application Status Update";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "After careful consideration, we regret to inform you that your application " +
                        "has not been successful at this time.\n\n" +
                        "Reason: %s\n\n" +
                        "We appreciate your interest in our organization and encourage you to " +
                        "apply again in 6 months if your circumstances change.\n\n" +
                        "Thank you,\nSecurity Recruitment Team",
                firstName, lastName, reason
        );
        sendSimpleEmail(email, subject, text);
    }

    // Verification Status Emails
    public void sendVerificationUpdate(String email, String firstName, String lastName,
                                       Map<String, String> statusUpdates) {
        StringBuilder statusText = new StringBuilder();
        statusUpdates.forEach((check, status) -> {
            statusText.append(String.format("- %s: %s\n", check, status));
        });

        String subject = "Verification Status Update";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "Your verification status has been updated:\n\n" +
                        "%s\n" +
                        "If any verifications require attention, our HR team will contact you.\n\n" +
                        "Regards,\nSecurity Compliance Team",
                firstName, lastName, statusText.toString()
        );
        sendSimpleEmail(email, subject, text);
    }

    // Document Expiry Notifications
    public void sendDocumentExpiryWarning(String email, String firstName, String lastName,
                                          String documentType, LocalDate expiryDate) {
        String subject = "Important: Document Expiring Soon";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "This is a reminder that your %s will expire on %s.\n\n" +
                        "Please submit a renewed document at least 2 weeks before expiration.\n\n" +
                        "Failure to maintain current documents may result in suspension.\n\n" +
                        "Regards,\nSecurity Compliance Team",
                firstName, lastName, documentType, expiryDate
        );
        sendSimpleEmail(email, subject, text);
    }

    // Shift Assignment Emails (from your original example)
    public void sendShiftAssignmentEmail(String to, String guardName, String shiftName,
                                         String siteName, LocalDate date, LocalTime startTime, LocalTime endTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("New Shift Assignment - " + shiftName);
        message.setText("Hello " + guardName + ",\n\n" +
                "You have been assigned to a new shift in the Security Operations Management System.\n\n" +
                "Shift Details:\n" +
                "Shift Name: " + shiftName + "\n" +
                "Site: " + siteName + "\n" +
                "Date: " + date + "\n" +
                "Start Time: " + startTime + "\n" +
                "End Time: " + endTime + "\n\n" +
                "Please ensure you arrive on time and bring all necessary equipment.\n" +
                "Contact your supervisor if you have any questions.\n\n" +
                "Regards,\nSOMS Admin Team");
        emailSender.send(message);
    }

    // Account Management Emails
    public void sendAccountCreationEmail(String to, String username, String password, String client) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your SOMS Account Has Been Created");
        message.setText("Hello,\n\n" +
                "Your account has been created in the Security Operations Management System.\n\n" +
                "Username: " + username + "\n" +
                "Temporary Password: " + password + "\n\n" +
                "Please log in and change your password as soon as possible.\n\n" +
                "Regards,\nSOMS Admin Team");
        emailSender.send(message);
    }

    // Bulk Operation Emails
    public void sendBulkOperationSummary(String email, String operationName,
                                         int successCount, int failureCount) {
        String subject = "Bulk Operation Complete: " + operationName;
        String text = String.format(
                "Hello,\n\n" +
                        "The bulk operation '%s' has been completed.\n\n" +
                        "Results:\n" +
                        "- Successful operations: %d\n" +
                        "- Failed operations: %d\n\n" +
                        "Please review the system logs for details on any failures.\n\n" +
                        "Regards,\nSOMS Admin Team",
                operationName, successCount, failureCount
        );
        sendSimpleEmail(email, subject, text);
    }


    public void sendAccountCreationEmail(String to, String username, String password, String role, String badgeNumber) {
        String subject = "Your Guard Account Has Been Created";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your guard account has been successfully created with the following details:\n\n" +
                        "Username: %s\n" +
                        "Temporary Password: %s\n" +
                        "Role: %s\n" +
                        "Badge Number: %s\n\n" +
                        "Please login and change your password immediately.\n\n" +
                        "Best regards,\n" +
                        "DEP Security Team",
                username, username, password, role, badgeNumber
        );

        sendSimpleEmail(to, subject, body);
    }

    public void sendGuardActivationEmail(String to, String fullName, String badgeNumber) {
        String subject = "Your Guard Status Has Been Activated";
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your guard status has been activated with the following details:\n\n" +
                        "Badge Number: %s\n" +
                        "Status: Active Guard\n\n" +
                        "You can now access all guard features in the system.\n\n" +
                        "Best regards,\n" +
                        "DEP Security Team",
                fullName, badgeNumber
        );

        sendSimpleEmail(to, subject, body);
    }

    // Add this method to your EmailService class
    public void sendPasswordResetEmail(String email, String firstName, String lastName, String newPassword) {
        String subject = "Password Reset - SOMS Security System";
        String text = String.format(
                "Dear %s %s,\n\n" +
                        "Your password has been reset as requested.\n\n" +
                        "Your new temporary password is: %s\n\n" +
                        "IMPORTANT SECURITY NOTICE:\n" +
                        "- Please log in immediately and change this password\n" +
                        "- This temporary password should only be used once\n" +
                        "- Do not share this password with anyone\n" +
                        "- For security reasons, please change it as soon as you log in\n\n" +
                        "If you did not request this password reset, please contact our support team immediately.\n\n" +
                        "Regards,\nSOMS Security Team",
                firstName, lastName, newPassword
        );
        sendSimpleEmail(email, subject, text);
    }

    /**
     * Sends patrol assignment notification to supervisor
     */
    public void sendPatrolAssignmentEmail(String toEmail, String fullName, String siteName,
                                          LocalDateTime startTime, LocalDateTime endTime, String notes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("New Patrol Assignment - " + siteName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        String emailContent = String.format(
                "Hello %s,\n\n" +
                        "You have been assigned a new patrol at %s.\n\n" +
                        "Patrol Details:\n" +
                        "Start Time: %s\n" +
                        "End Time: %s\n\n" +
                        "Notes: %s\n\n" +
                        "Checkpoints will be available in the web app once you start the patrol.\n\n" +
                        "Please log in to the system to view complete details.\n\n" +
                        "Best regards,\n" +
                        "Security Operations Management System",
                fullName,
                siteName,
                startTime.format(formatter),
                endTime.format(formatter),
                notes != null ? notes : "None"
        );

        message.setText(emailContent);
        emailSender.send(message);
    }


}