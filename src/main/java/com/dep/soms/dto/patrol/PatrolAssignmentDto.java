package com.dep.soms.dto.patrol;

import com.dep.soms.dto.site.SiteDto;
import com.dep.soms.dto.user.UserDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatrolAssignmentDto {
    private Long id;
    private Long patrolId;
    private Long supervisorId;
    private UserDto supervisor;
    private String supervisorName;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime declinedAt;
    private String declineReason;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PatrolDto patrolDetails;
    //private SiteDto site;
    private SiteDto primarySite;  // renamed from 'site' to be more explicit
    private List<SiteDto> sites;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate assignmentDate;
}
