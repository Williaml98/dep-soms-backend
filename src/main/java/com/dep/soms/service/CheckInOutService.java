package com.dep.soms.service;

import com.dep.soms.dto.shift.CheckInRequest;
import com.dep.soms.dto.shift.CheckOutRequest;
import com.dep.soms.dto.shift.ShiftAssignmentDto;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.ShiftAssignment;
import com.dep.soms.repository.ShiftAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class CheckInOutService {
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    public CheckInOutService(ShiftAssignmentRepository shiftAssignmentRepository) {
        this.shiftAssignmentRepository = shiftAssignmentRepository;
    }

//      @Transactional
//    public ShiftAssignmentDto checkIn(CheckInRequest request) {
//        ShiftAssignment assignment = shiftAssignmentRepository.findById(request.getShiftAssignmentId())
//                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found with id: " + request.getShiftAssignmentId()));
//
//        if (assignment.getCheckInTime() != null) {
//            throw new IllegalStateException("Already checked in");
//        }
//
//        assignment.setCheckInTime(LocalDateTime.from(LocalTime.now()));
//
//        // Format location string
//        String location = request.getLocation();
//        if (request.getLatitude() != null && request.getLongitude() != null) {
//            location = location + " (" + request.getLatitude() + "," + request.getLongitude() + ")";
//        }
//
//        assignment.setCheckInLocation(location);
//
//        if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.SCHEDULED) {
//            assignment.setStatus(ShiftAssignment.AssignmentStatus.IN_PROGRESS);
//        }
//
//        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);
//        return mapToDto(updatedAssignment);
//    }
//
//    @Transactional
//    public ShiftAssignmentDto checkOut(CheckOutRequest request) {
//        ShiftAssignment assignment = shiftAssignmentRepository.findById(request.getShiftAssignmentId())
//                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found with id: " + request.getShiftAssignmentId()));
//
//        if (assignment.getCheckInTime() == null) {
//            throw new IllegalStateException("Not checked in yet");
//        }
//
//        if (assignment.getCheckOutTime() != null) {
//            throw new IllegalStateException("Already checked out");
//        }
//
//        assignment.setCheckOutTime(LocalDateTime.from(LocalTime.now()));
//
//        // Format location string
//        String location = request.getLocation();
//        if (request.getLatitude() != null && request.getLongitude() != null) {
//            location = location + " (" + request.getLatitude() + "," + request.getLongitude() + ")";
//        }
//
//        assignment.setCheckOutLocation(location);
//        assignment.setStatus(ShiftAssignment.AssignmentStatus.COMPLETED);
//
//        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);
//        return mapToDto(updatedAssignment);
//    }

    @Transactional
    public ShiftAssignmentDto checkIn(CheckInRequest request) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(request.getShiftAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found with id: " + request.getShiftAssignmentId()));

        if (assignment.getCheckInTime() != null) {
            throw new IllegalStateException("Already checked in");
        }

        assignment.setCheckInTime(LocalDateTime.now());

        // Set the check-in latitude and longitude directly
        if (request.getLatitude() != null && request.getLongitude() != null) {
            assignment.setCheckInLatitude(request.getLatitude());
            assignment.setCheckInLongitude(request.getLongitude());
        }

        if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.SCHEDULED) {
            assignment.setStatus(ShiftAssignment.AssignmentStatus.IN_PROGRESS);
        }

        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);
        return mapToDto(updatedAssignment);
    }

    @Transactional
    public ShiftAssignmentDto checkOut(CheckOutRequest request) {
        ShiftAssignment assignment = shiftAssignmentRepository.findById(request.getShiftAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift assignment not found with id: " + request.getShiftAssignmentId()));

        if (assignment.getCheckInTime() == null) {
            throw new IllegalStateException("Not checked in yet");
        }

        if (assignment.getCheckOutTime() != null) {
            throw new IllegalStateException("Already checked out");
        }

        assignment.setCheckOutTime(LocalDateTime.now());

        // Set the check-out latitude and longitude directly
        if (request.getLatitude() != null && request.getLongitude() != null) {
            assignment.setCheckOutLatitude(request.getLatitude());
            assignment.setCheckOutLongitude(request.getLongitude());
        }

        assignment.setStatus(ShiftAssignment.AssignmentStatus.COMPLETED);

        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);
        return mapToDto(updatedAssignment);
    }


private ShiftAssignmentDto mapToDto(ShiftAssignment assignment) {
    return ShiftAssignmentDto.builder()
            .id(assignment.getId())
            .shiftId(assignment.getShift().getId())
            .shiftName(assignment.getShift().getName())
            .guardId(assignment.getGuard().getId())
            .guardName(assignment.getGuard().getUser().getFirstName() + " " + assignment.getGuard().getUser().getLastName())
            .siteId(assignment.getShift().getSite().getId())
            .siteName(assignment.getShift().getSite().getName())
            .date(assignment.getAssignmentDate())
            .startTime(assignment.getStartTime() != null ? LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime())
            .endTime(assignment.getEndTime() != null ? LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime())
            .status(assignment.getStatus())
            .notes(assignment.getNotes())
            .checkInTime(assignment.getCheckInTime() != null ? LocalTime.from(assignment.getCheckInTime()) : null)
            .checkOutTime(assignment.getCheckOutTime() != null ? LocalTime.from(assignment.getCheckOutTime()) : null)
            .checkInLatitude(assignment.getCheckInLatitude())
            .checkInLongitude(assignment.getCheckInLongitude())
            .checkOutLatitude(assignment.getCheckOutLatitude())
            .checkOutLongitude(assignment.getCheckOutLongitude())
            .build();
}

}
