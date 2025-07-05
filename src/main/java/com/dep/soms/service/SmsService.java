package com.dep.soms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    // This is a placeholder for an actual SMS service implementation
    // You would typically use a service like Twilio, Nexmo, etc.
    public void sendAccountCreationSms(String phoneNumber, String username, String role) {
        try {
            // In a real implementation, you would call your SMS provider's API here
            logger.info("Sending SMS to {}: Your SOMS {} account has been created with username: {}. Check your email for details.",
                    phoneNumber, role, username);

            // Example Twilio-like implementation (commented out):
            /*
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(FROM_NUMBER),
                    "Your SOMS " + role + " account has been created with username: " + username +
                    ". Check your email for details and login to change your password.")
                .create();
            */
        } catch (Exception e) {
            logger.error("Failed to send account creation SMS to: {}", phoneNumber, e);
        }
    }
}
