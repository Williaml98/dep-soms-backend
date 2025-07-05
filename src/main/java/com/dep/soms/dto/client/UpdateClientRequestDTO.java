package com.dep.soms.dto.client;

import com.dep.soms.model.ClientRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClientRequestDTO {
    private ClientRequest.RequestStatus status;
    private Long assignedToId;
    private String resolutionNotes;
}

