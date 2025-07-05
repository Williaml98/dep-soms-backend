//package com.dep.soms.dto.incident;
//
//import com.dep.soms.model.Incident;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class IncidentDto {
//    private Long id;
//    private Long siteId;
//    private String siteName;
//    private Long guardId;
//    private String guardName;
//    private String incidentType;
//    private String title;
//    private String description;
//    private LocalDateTime incidentTime;
//    private String location;
//    private Double longitude;
//    private Double latitude;
////    private Incident.IncidentSeverity severity;
//private Incident.IncidentSeverity severity; // ENUM
//    private Incident.IncidentStatus status;
//
//    //    private Incident.IncidentStatus status;
//    private String actionTaken;
////    private String[] images;
//    private String images;
//    private String[] involvedParties;
//    private String reportedTo;
//    private LocalDateTime reportedTime;
//}

package com.dep.soms.dto.incident;

import com.dep.soms.model.Incident;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto {

    private Long id;
    private Long siteId;
    private String siteName;
    private String siteAddress;
    private Long guardId;
    private String guardName;
    private Long shiftAssignmentId;
    private String shiftName;
    private Long reportedByUserId;
    private String reportedByUserName;

    private Incident.IncidentType incidentType;
    private String title;
    private String description;
    private LocalDateTime incidentTime;
    private String locationDetails;
    private Double latitude;
    private Double longitude;

    private Incident.IncidentSeverity severity;
    private Incident.IncidentStatus status;

    private String actionTaken;
    private String involvedParties;
    private String reportedTo;

    // Multiple attachments
    private List<AttachmentDto> attachments;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
