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
public class MessageDto {
    private Long id;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private Long senderId;
    private String senderName;
    private Long conversationId;
    private boolean isFromCurrentUser;
}


