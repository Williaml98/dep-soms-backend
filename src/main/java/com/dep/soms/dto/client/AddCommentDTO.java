package com.dep.soms.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentDTO {
    private Long clientRequestId;
    private String comment;
    private Long userId;
}
