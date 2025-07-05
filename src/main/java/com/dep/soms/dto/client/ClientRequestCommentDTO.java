package com.dep.soms.dto.client;

import com.dep.soms.model.ClientRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestCommentDTO {
    private Long id;
    private Long clientRequestId;
    private Long userId;
    private String userName;
    private String userProfilePicture;
    private String comment;
    private LocalDateTime createdAt;
}


