package com.dep.soms.service;

import com.dep.soms.dto.report.GuardPerformanceDetailDto;
import com.dep.soms.dto.report.GuardPerformanceReportDto;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Guard;
import com.dep.soms.model.Incident;
import com.dep.soms.model.ShiftAssignment;
import com.dep.soms.repository.GuardRepository;
import com.dep.soms.repository.IncidentRepository;
import com.dep.soms.repository.ShiftAssignmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GuardPerformanceService {

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Transactional
    public GuardPerformanceReportDto generateGuardPerformanceReport(Long guardId, LocalDate startDate, LocalDate endDate) {
        List<Guard> guards;
        String guardName = null;
        if (guardId != null) {
            Guard guard = guardRepository.findById(guardId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));
            guards = Collections.singletonList(guard);
            guardName = guard.getUser().getFirstName() + " " + guard.getUser().getLastName();
        } else {
            guards = guardRepository.findAll();
        }

        int totalShifts = 0;
        int completedShifts = 0;
        int missedShifts = 0;
        int lateArrivals = 0;
        int earlyDepartures = 0;
        int totalIncidents = 0;
        List<GuardPerformanceDetailDto> performanceByWeek = new ArrayList<>();

        // Process data by week
        LocalDate currentWeekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate currentWeekEnd = currentWeekStart.plusDays(6);

        while (!currentWeekStart.isAfter(endDate)) {
            int weekShifts = 0;
            int weekCompletedShifts = 0;
            int weekMissedShifts = 0;
            int weekLateArrivals = 0;
            int weekIncidents = 0;

            for (Guard guard : guards) {
                // Get shifts for this guard in this week
                List<ShiftAssignment> weekAssignments = shiftAssignmentRepository.findByGuardAndAssignmentDateBetween(
                        guard, currentWeekStart, currentWeekEnd);

                weekShifts += weekAssignments.size();

                for (ShiftAssignment assignment : weekAssignments) {
                    if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
                        weekCompletedShifts++;
                        LocalTime scheduledStart = assignment.getStartTime() != null ?
                                LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime();

                        // Check if late
//                        if (assignment.getCheckInTime() != null &&
//                                assignment.getCheckInTime().isAfter(ChronoLocalDateTime.from(scheduledStart.plusMinutes(5)))) {
//                            weekLateArrivals++;
//                        }
                        if (assignment.getCheckInTime() != null) {
                            LocalTime checkInTime = assignment.getCheckInTime().toLocalTime();
                            if (checkInTime.isAfter(scheduledStart.plusMinutes(5))) {
                                weekLateArrivals++;
                            }
                        }
                    } else if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.MISSED) {
                        weekMissedShifts++;
                    }
                }

                // Get incidents for this guard in this week
                LocalDateTime weekStartDateTime = currentWeekStart.atStartOfDay();
                LocalDateTime weekEndDateTime = currentWeekEnd.plusDays(1).atStartOfDay();
                List<Incident> weekIncidentsList = incidentRepository.findByGuardAndIncidentTimeBetween(
                        guard, weekStartDateTime, weekEndDateTime);
                weekIncidents += weekIncidentsList.size();
            }

            totalShifts += weekShifts;
            completedShifts += weekCompletedShifts;
            missedShifts += weekMissedShifts;
            lateArrivals += weekLateArrivals;
            totalIncidents += weekIncidents;

            double weekAttendanceRate = weekShifts > 0 ?
                    (double) weekCompletedShifts / weekShifts * 100 : 0;

            GuardPerformanceDetailDto weekDetail = GuardPerformanceDetailDto.builder()
                    .weekStartDate(currentWeekStart)
                    .weekEndDate(currentWeekEnd)
                    .totalShifts(weekShifts)
                    .completedShifts(weekCompletedShifts)
                    .missedShifts(weekMissedShifts)
                    .lateArrivals(weekLateArrivals)
                    .attendanceRate(weekAttendanceRate)
                    .incidents(weekIncidents)
                    .build();

            performanceByWeek.add(weekDetail);

            // Move to next week
            currentWeekStart = currentWeekStart.plusWeeks(1);
            currentWeekEnd = currentWeekEnd.plusWeeks(1);
        }

        double attendanceRate = totalShifts > 0 ?
                (double) completedShifts / totalShifts * 100 : 0;
        double averageIncidentsPerShift = completedShifts > 0 ?
                (double) totalIncidents / completedShifts : 0;

        return GuardPerformanceReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .guardId(guardId)
                .guardName(guardName)
                .totalShifts(totalShifts)
                .completedShifts(completedShifts)
                .missedShifts(missedShifts)
                .lateArrivals(lateArrivals)
                .earlyDepartures(earlyDepartures)
                .attendanceRate(attendanceRate)
                .totalIncidents(totalIncidents)
                .averageIncidentsPerShift(averageIncidentsPerShift)
                .performanceByWeek(performanceByWeek)
                .build();
    }

    @Transactional
    public List<GuardPerformanceReportDto> generateTeamPerformanceReport(LocalDate startDate, LocalDate endDate) {
        List<Guard> allGuards = guardRepository.findAll();
        List<GuardPerformanceReportDto> teamReports = new ArrayList<>();

        for (Guard guard : allGuards) {
            GuardPerformanceReportDto guardReport = generateGuardPerformanceReport(guard.getId(), startDate, endDate);
            teamReports.add(guardReport);
        }

        return teamReports;
    }

    @Transactional
    public GuardPerformanceReportDto getTopPerformer(LocalDate startDate, LocalDate endDate) {
        List<GuardPerformanceReportDto> allReports = generateTeamPerformanceReport(startDate, endDate);

        return allReports.stream()
                .filter(report -> report.getTotalShifts() > 0) // Only consider guards with assigned shifts
                .max((r1, r2) -> {
                    // First compare attendance rate
                    int attendanceComparison = Double.compare(r1.getAttendanceRate(), r2.getAttendanceRate());
                    if (attendanceComparison != 0) {
                        return attendanceComparison;
                    }

                    // If attendance rates are equal, compare by fewer late arrivals
                    int lateArrivalsComparison = Integer.compare(r2.getLateArrivals(), r1.getLateArrivals());
                    if (lateArrivalsComparison != 0) {
                        return lateArrivalsComparison;
                    }

                    // If still tied, compare by more completed shifts
                    return Integer.compare(r1.getCompletedShifts(), r2.getCompletedShifts());
                })
                .orElse(null);
    }
}
