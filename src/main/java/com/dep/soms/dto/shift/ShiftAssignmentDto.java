package com.dep.soms.dto.shift;

import com.dep.soms.model.ShiftAssignment;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignmentDto {
    private Long id;
    private Long shiftId;
    private String shiftName;
    private Long guardId;
    private String guardName;
    private Long siteId;
    private String siteName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private ShiftAssignment.AssignmentStatus status;
    private String notes;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private Double checkInLatitude;
    private Double checkInLongitude;
    private Double checkOutLatitude;
    private Double checkOutLongitude;


    private GuardDto guard;
    private SiteDto site;
    private ShiftDto shift;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuardDto {
        private Long id;
        private UserDto user;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class UserDto {
            private Long id;
            private String firstName;
            private String lastName;
            private String email;
            private String profilePicture;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SiteDto {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShiftDto {
        private Long id;
        private String name;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}
