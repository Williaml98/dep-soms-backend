//package com.dep.soms.dto.incident;
//
//import com.dep.soms.model.Incident;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//public class IncidentRequest {
//
//    @NotNull
//    private Long siteId;
//
//    @NotNull
//    private Long guardId;
//
//    @NotBlank
//    private String incidentType;
//
//    @NotBlank
//    private String title;
//
//    @NotBlank
//    private String description;
//
//    @NotNull
//    private LocalDateTime incidentTime;
//
//    private String location;
//
//    @NotNull
//    private Incident.IncidentSeverity severity;
////
//    private Incident.IncidentStatus status = Incident.IncidentStatus.REPORTED;
//
//    private String actionTaken;
//
//    private String[] images;
//
//    private String[] involvedParties;
//
//    private String reportedTo;
//
//    private LocalDateTime reportedTime;
//    private Double getLongitude;
//    private Double getLatitude;
//}


package com.dep.soms.dto.incident;

import com.dep.soms.model.Incident;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class IncidentRequest {

    @NotNull(message = "Incident type is required")
    private Incident.IncidentType incidentType;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Incident time is required")
    private LocalDateTime incidentTime;

    @NotNull(message = "Severity is required")
    private Incident.IncidentSeverity severity;

    // Location details within the site (optional)
    private String locationDetails;

    // GPS coordinates (optional)
    private Double latitude;
    private Double longitude;

    private String status;

    // Multiple file uploads support
    private List<MultipartFile> attachments;

    // Optional fields
    private String actionTaken;
    private String involvedParties;
    private String reportedTo;
}
