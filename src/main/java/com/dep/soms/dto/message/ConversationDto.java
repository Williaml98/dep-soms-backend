package com.dep.soms.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private Long id;
    private String subject;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private Long clientId;
    private String clientName;
    private Long adminId;
    private String adminName;
    private int unreadCount;
    private MessageDto lastMessage;
}
