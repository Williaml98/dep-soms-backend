//package com.dep.soms.dto.message;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ConversationRequest {
//    @NotBlank(message = "Subject is required")
//    @Size(max = 255, message = "Subject must be less than 255 characters")
//    private String subject;
//    @NotBlank(message = "Initial message is required")
//    private String initialMessage;
//}

package com.dep.soms.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ConversationRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject must not exceed 255 characters")
    private String subject;

    @Size(max = 1000, message = "Initial message must not exceed 1000 characters")
    private String initialMessage;

    // For admin-created conversations
    private Long clientId;

    // Getters and setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getInitialMessage() {
        return initialMessage;
    }

    public void setInitialMessage(String initialMessage) {
        this.initialMessage = initialMessage;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}
