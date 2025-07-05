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
//public class CheckInOutRequest {
//    private Double latitude;
//    private Double longitude;
//    private String notes;
//}

package com.dep.soms.dto.shift;

public class CheckInOutRequest {
    private Double latitude;
    private Double longitude;
    private Double gpsAccuracy;  // Add this field
    private String notes;

    // Constructors
    public CheckInOutRequest() {}

    public CheckInOutRequest(Double latitude, Double longitude, Double gpsAccuracy, String notes) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.gpsAccuracy = gpsAccuracy;
        this.notes = notes;
    }

    // Getters and Setters
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getGpsAccuracy() { return gpsAccuracy; }
    public void setGpsAccuracy(Double gpsAccuracy) { this.gpsAccuracy = gpsAccuracy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
