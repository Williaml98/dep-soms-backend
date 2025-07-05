//package com.dep.soms.dto.shift;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class LocationVerificationResponse {
//    private boolean atCorrectLocation;
//    private String message;
//    private ShiftAssignmentDto shiftAssignment;
//}

package com.dep.soms.dto.shift;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationVerificationResponse {
    private boolean atCorrectLocation;
    private String message;
    private double distanceFromSite;
    private ShiftAssignmentDto shiftAssignment;

    public LocationVerificationResponse(boolean atCorrectLocation, String message) {
        this.atCorrectLocation = atCorrectLocation;
        this.message = message;
        this.distanceFromSite = 0.0;
        this.shiftAssignment = null;
    }
}
