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
public class CreateClientRequestDTO {
    private Long clientId;
    private Long siteId;
    private String title;
    private String description;
    private ClientRequest.RequestPriority priority;
}
