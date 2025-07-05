package com.dep.soms.dto.client;

import com.dep.soms.model.ClientRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long siteId;
    private String siteName;
    private Long requestedById;
    private String requestedByName;
    private String title;
    private String description;
    private ClientRequest.RequestPriority priority;
    private ClientRequest.RequestStatus status;
    private Long assignedToId;
    private String assignedToName;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
    private List<ClientRequestCommentDTO> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


