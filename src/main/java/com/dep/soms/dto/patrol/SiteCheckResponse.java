package com.dep.soms.dto.patrol;

public class SiteCheckResponse {
    private boolean verified;
    private String message;
    private Double distance;
    private PatrolAssignmentDto assignment;

    // Getters and setters
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public PatrolAssignmentDto getAssignment() { return assignment; }
    public void setAssignment(PatrolAssignmentDto assignment) { this.assignment = assignment; }
}