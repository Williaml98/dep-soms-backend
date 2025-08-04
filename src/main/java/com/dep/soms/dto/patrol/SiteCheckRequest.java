package com.dep.soms.dto.patrol;

public class SiteCheckRequest {
    private Double latitude;
    private Double longitude;
    private Double gpsAccuracy;
    private String notes;

    // Getters and setters
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getGpsAccuracy() { return gpsAccuracy; }
    public void setGpsAccuracy(Double gpsAccuracy) { this.gpsAccuracy = gpsAccuracy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}