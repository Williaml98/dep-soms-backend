//package com.dep.soms.service;
//
//import com.dep.soms.dto.report.*;
//import com.dep.soms.exception.ResourceNotFoundException;
//import com.dep.soms.model.*;
//import com.dep.soms.repository.*;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.chrono.ChronoLocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.time.temporal.TemporalAdjusters;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class ReportService {
//    @Autowired
//    private ShiftAssignmentRepository shiftAssignmentRepository;
//
//    @Autowired
//    private IncidentRepository incidentRepository;
//
//    @Autowired
//    private SiteRepository siteRepository;
//
//    @Autowired
//    private GuardRepository guardRepository;
//
//    @Autowired
//    private ClientRepository clientRepository;
//@Transactional
//    public AttendanceReportDto generateAttendanceReport(Long siteId, Long guardId, LocalDate startDate, LocalDate endDate) {
//        List<ShiftAssignment> assignments;
//
//        if (siteId != null && guardId != null) {
//            assignments = shiftAssignmentRepository.findByShift_Site_IdAndGuard_IdAndAssignmentDateBetween(
//                    siteId, guardId, startDate, endDate);
//        } else if (siteId != null) {
//            assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDateBetween(
//                    siteId, startDate, endDate);
//        } else if (guardId != null) {
//            assignments = shiftAssignmentRepository.findByGuard_IdAndAssignmentDateBetween(
//                    guardId, startDate, endDate);
//        } else {
//            assignments = shiftAssignmentRepository.findByAssignmentDateBetween(startDate, endDate);
//        }
//
//        int totalShifts = assignments.size();
//        int completedShifts = 0;
//        int missedShifts = 0;
//        int lateArrivals = 0;
//        int earlyDepartures = 0;
//
//        List<AttendanceRecordDto> records = new ArrayList<>();
//
//        for (ShiftAssignment assignment : assignments) {
//            LocalTime scheduledStart = assignment.getStartTime() != null ?
//                    LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime();
//            LocalTime scheduledEnd = assignment.getEndTime() != null ?
//                    LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime();
//
//            boolean isLate = false;
//            boolean isEarlyDeparture = false;
//            long minutesLate = 0;
//            long minutesEarlyDeparture = 0;
//
//            if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
//                completedShifts++;
//
//                // Check if late
//                if (assignment.getCheckInTime() != null &&
//                        assignment.getCheckInTime().isAfter(ChronoLocalDateTime.from(scheduledStart.plusMinutes(5)))) {
//                    isLate = true;
//                    lateArrivals++;
//                    minutesLate = ChronoUnit.MINUTES.between(scheduledStart, assignment.getCheckInTime());
//                }
//
//                // Check if early departure
//                if (assignment.getCheckOutTime() != null &&
//                        assignment.getCheckOutTime().isBefore(ChronoLocalDateTime.from(scheduledEnd.minusMinutes(5)))) {
//                    isEarlyDeparture = true;
//                    earlyDepartures++;
//                    minutesEarlyDeparture = ChronoUnit.MINUTES.between(assignment.getCheckOutTime(), scheduledEnd);
//                }
//            } else if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.MISSED) {
//                missedShifts++;
//            }
//
//            AttendanceRecordDto record = AttendanceRecordDto.builder()
//                    .assignmentId(assignment.getId())
//                    .shiftId(assignment.getShift().getId())
//                    .shiftName(assignment.getShift().getName())
//                    .guardId(assignment.getGuard().getId())
//                    .guardName(assignment.getGuard().getUser().getFirstName() + " " +
//                            assignment.getGuard().getUser().getLastName())
//                    .siteId(assignment.getShift().getSite().getId())
//                    .siteName(assignment.getShift().getSite().getName())
//                    .date(assignment.getAssignmentDate())
//                    .scheduledStartTime(scheduledStart)
//                    .scheduledEndTime(scheduledEnd)
//                    .actualStartTime(LocalTime.from(assignment.getCheckInTime()))
//                    .actualEndTime(LocalTime.from(assignment.getCheckOutTime()))
//                    .status(assignment.getStatus())
//                    .isLate(isLate)
//                    .isEarlyDeparture(isEarlyDeparture)
//                    .minutesLate(minutesLate)
//                    .minutesEarlyDeparture(minutesEarlyDeparture)
//                    .build();
//
//            records.add(record);
//        }
//
//        double attendanceRate = totalShifts > 0 ?
//                (double) completedShifts / totalShifts * 100 : 0;
//
//        String siteName = null;
//        if (siteId != null) {
//            Site site = siteRepository.findById(siteId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
//            siteName = site.getName();
//        }
//
//        String guardName = null;
//        if (guardId != null) {
//            Guard guard = guardRepository.findById(guardId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));
//            guardName = guard.getUser().getFirstName() + " " + guard.getUser().getLastName();
//        }
//
//        return AttendanceReportDto.builder()
//                .startDate(startDate)
//                .endDate(endDate)
//                .siteId(siteId)
//                .siteName(siteName)
//                .guardId(guardId)
//                .guardName(guardName)
//                .totalShifts(totalShifts)
//                .completedShifts(completedShifts)
//                .missedShifts(missedShifts)
//                .lateArrivals(lateArrivals)
//                .earlyDepartures(earlyDepartures)
//                .attendanceRate(attendanceRate)
//                .records(records)
//                .build();
//    }
//@Transactional
//    public IncidentReportDto generateIncidentReport(Long siteId, LocalDate startDate, LocalDate endDate) {
//        LocalDateTime startDateTime = startDate.atStartOfDay();
//        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
//
//        List<Incident> incidents;
//
//        if (siteId != null) {
//            incidents = incidentRepository.findBySite_IdAndIncidentTimeBetween(
//                    siteId, startDateTime, endDateTime);
//        } else {
//            incidents = incidentRepository.findByIncidentTimeBetween(startDateTime, endDateTime);
//        }
//
//        int totalIncidents = incidents.size();
//
//        // Count incidents by type
//        Map<String, Integer> incidentsByType = new HashMap<>();
//        for (Incident incident : incidents) {
//            incidentsByType.put(
//                    String.valueOf(incident.getIncidentType()),
//                    incidentsByType.getOrDefault(incident.getIncidentType(), 0) + 1);
//        }
//
//        // Count incidents by severity
//        Map<String, Integer> incidentsBySeverity = new HashMap<>();
//        for (Incident incident : incidents) {
//            incidentsBySeverity.put(
//                    incident.getSeverity().name(),
//                    incidentsBySeverity.getOrDefault(incident.getSeverity().name(), 0) + 1);
//        }
//
//        List<IncidentSummaryDto> incidentSummaries = incidents.stream()
//                .map(incident -> IncidentSummaryDto.builder()
//                        .id(incident.getId())
//                        .title(incident.getTitle())
//                        .incidentType(String.valueOf(incident.getIncidentType()))
//                        .incidentTime(incident.getIncidentTime())
//                        .siteName(incident.getSite().getName())
//                .guardName(incident.getGuard().getUser().getFirstName() + " " +
//                        incident.getGuard().getUser().getLastName())
//                        .severity(incident.getSeverity()) // PASS ENUM DIRECTLY
//                        .status(incident.getStatus())
//                        .build())
//                .collect(Collectors.toList());
//
//        String siteName = null;
//        if (siteId != null) {
//            Site site = siteRepository.findById(siteId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
//            siteName = site.getName();
//        }
//
//        return IncidentReportDto.builder()
//                .startDate(startDate)
//                .endDate(endDate)
//                .siteId(siteId)
//                .siteName(siteName)
//                .totalIncidents(totalIncidents)
//                .incidentsByType(incidentsByType)
//                .incidentsBySeverity(incidentsBySeverity)
//                .incidents(incidentSummaries)
//                .build();
//    }
//    @Transactional
//
//    public SiteCoverageReportDto generateSiteCoverageReport(Long clientId, LocalDate startDate, LocalDate endDate) {
//        List<Site> sites;
//        String clientName = null;
//
//        if (clientId != null) {
//            Client client = clientRepository.findById(clientId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
//            sites = siteRepository.findByClient(client);
//            clientName = client.getCompanyName();
//        } else {
//            sites = siteRepository.findAll();
//        }
//
//        int totalSites = sites.size();
//        int totalShifts = 0;
//        int totalHours = 0;
//        int coveredHours = 0;
//
//        List<SiteCoverageDetailDto> siteDetails = new ArrayList<>();
//
//        for (Site site : sites) {
//            List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDateBetween(
//                    site.getId(), startDate, endDate);
//
//            int siteShifts = assignments.size();
//            int siteHours = 0;
//            int siteCoveredHours = 0;
//
//            for (ShiftAssignment assignment : assignments) {
//                LocalTime startTime = assignment.getStartTime() != null ?
//                        LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime();
//                LocalTime endTime = assignment.getEndTime() != null ?
//                        LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime();
//
//                int shiftHours = (int) ChronoUnit.HOURS.between(startTime, endTime);
//                if (shiftHours < 0) {
//                    // Handle overnight shifts
//                    shiftHours += 24;
//                }
//
//                siteHours += shiftHours;
//
//                if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
//                    siteCoveredHours += shiftHours;
//                }
//            }
//
//            totalShifts += siteShifts;
//            totalHours += siteHours;
//            coveredHours += siteCoveredHours;
//
//            double siteCoverageRate = siteHours > 0 ?
//                    (double) siteCoveredHours / siteHours * 100 : 0;
//
//            SiteCoverageDetailDto detail = SiteCoverageDetailDto.builder()
//                    .siteId(site.getId())
//                    .siteName(site.getName())
//                    .totalShifts(siteShifts)
//                    .totalHours(siteHours)
//                    .coveredHours(siteCoveredHours)
//                    .coverageRate(siteCoverageRate)
//                    .build();
//
//            siteDetails.add(detail);
//        }
//
//        double coverageRate = totalHours > 0 ?
//                (double) coveredHours / totalHours * 100 : 0;
//
//        return SiteCoverageReportDto.builder()
//                .startDate(startDate)
//                .endDate(endDate)
//                .clientId(clientId)
//                .clientName(clientName)
//                .totalSites(totalSites)
//                .totalShifts(totalShifts)
//                .totalHours(totalHours)
//                .coveredHours(coveredHours)
//                .coverageRate(coverageRate)
//                .siteDetails(siteDetails)
//                .build();
//    }
//@Transactional
//    public GuardPerformanceReportDto generateGuardPerformanceReport(Long guardId, LocalDate startDate, LocalDate endDate) {
//        List<Guard> guards;
//        String guardName = null;
//
//        if (guardId != null) {
//            Guard guard = guardRepository.findById(guardId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));
//            guards = Collections.singletonList(guard);
//            guardName = guard.getUser().getFirstName() + " " + guard.getUser().getLastName();
//        } else {
//            guards = guardRepository.findAll();
//        }
//
//        int totalShifts = 0;
//        int completedShifts = 0;
//        int missedShifts = 0;
//        int lateArrivals = 0;
//        int earlyDepartures = 0;
//        int totalIncidents = 0;
//
//        List<GuardPerformanceDetailDto> performanceByWeek = new ArrayList<>();
//
//        // Process data by week
//        LocalDate currentWeekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
//        LocalDate currentWeekEnd = currentWeekStart.plusDays(6);
//
//        while (!currentWeekStart.isAfter(endDate)) {
//            int weekShifts = 0;
//            int weekCompletedShifts = 0;
//            int weekMissedShifts = 0;
//            int weekLateArrivals = 0;
//            int weekIncidents = 0;
//
//            for (Guard guard : guards) {
//                // Get shifts for this guard in this week
//                List<ShiftAssignment> weekAssignments = shiftAssignmentRepository.findByGuardAndAssignmentDateBetween(
//                        guard, currentWeekStart, currentWeekEnd);
//
//                weekShifts += weekAssignments.size();
//
//                for (ShiftAssignment assignment : weekAssignments) {
//                    if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
//                        weekCompletedShifts++;
//
//                        LocalTime scheduledStart = assignment.getStartTime() != null ?
//                                LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime();
//
//                        // Check if late
//                        if (assignment.getCheckInTime() != null &&
//                                assignment.getCheckInTime().isAfter(ChronoLocalDateTime.from(scheduledStart.plusMinutes(5)))) {
//                            weekLateArrivals++;
//                        }
//                    } else if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.MISSED) {
//                        weekMissedShifts++;
//                    }
//                }
//
//                // Get incidents for this guard in this week
//                LocalDateTime weekStartDateTime = currentWeekStart.atStartOfDay();
//                LocalDateTime weekEndDateTime = currentWeekEnd.plusDays(1).atStartOfDay();
//
//                List<Incident> weekIncidentsList = incidentRepository.findByGuardAndIncidentTimeBetween(
//                        guard, weekStartDateTime, weekEndDateTime);
//
//                weekIncidents += weekIncidentsList.size();
//            }
//
//            totalShifts += weekShifts;
//            completedShifts += weekCompletedShifts;
//            missedShifts += weekMissedShifts;
//            lateArrivals += weekLateArrivals;
//            totalIncidents += weekIncidents;
//
//            double weekAttendanceRate = weekShifts > 0 ?
//                    (double) weekCompletedShifts / weekShifts * 100 : 0;
//
//            GuardPerformanceDetailDto weekDetail = GuardPerformanceDetailDto.builder()
//                    .weekStartDate(currentWeekStart)
//                    .weekEndDate(currentWeekEnd)
//                    .totalShifts(weekShifts)
//                    .completedShifts(weekCompletedShifts)
//                    .missedShifts(weekMissedShifts)
//                    .lateArrivals(weekLateArrivals)
//                    .attendanceRate(weekAttendanceRate)
//                    .incidents(weekIncidents)
//                    .build();
//
//            performanceByWeek.add(weekDetail);
//
//            // Move to next week
//            currentWeekStart = currentWeekStart.plusWeeks(1);
//            currentWeekEnd = currentWeekEnd.plusWeeks(1);
//        }
//
//        double attendanceRate = totalShifts > 0 ?
//                (double) completedShifts / totalShifts * 100 : 0;
//
//        double averageIncidentsPerShift = completedShifts > 0 ?
//                (double) totalIncidents / completedShifts : 0;
//
//        return GuardPerformanceReportDto.builder()
//                .startDate(startDate)
//                .endDate(endDate)
//                .guardId(guardId)
//                .guardName(guardName)
//                .totalShifts(totalShifts)
//                .completedShifts(completedShifts)
//                .missedShifts(missedShifts)
//                .lateArrivals(lateArrivals)
//                .earlyDepartures(earlyDepartures)
//                .attendanceRate(attendanceRate)
//                .totalIncidents(totalIncidents)
//                .averageIncidentsPerShift(averageIncidentsPerShift)
//                .performanceByWeek(performanceByWeek)
//                .build();
//    }
//}

package com.dep.soms.service;

import com.dep.soms.dto.report.*;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Transactional
    public AttendanceReportDto generateAttendanceReport(Long siteId, Long guardId, LocalDate startDate, LocalDate endDate) {
        List<ShiftAssignment> assignments;
        if (siteId != null && guardId != null) {
            assignments = shiftAssignmentRepository.findByShift_Site_IdAndGuard_IdAndAssignmentDateBetween(
                    siteId, guardId, startDate, endDate);
        } else if (siteId != null) {
            assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDateBetween(
                    siteId, startDate, endDate);
        } else if (guardId != null) {
            assignments = shiftAssignmentRepository.findByGuard_IdAndAssignmentDateBetween(
                    guardId, startDate, endDate);
        } else {
            assignments = shiftAssignmentRepository.findByAssignmentDateBetween(startDate, endDate);
        }

        int totalShifts = assignments.size();
        int completedShifts = 0;
        int missedShifts = 0;
        int lateArrivals = 0;
        int earlyDepartures = 0;
        List<AttendanceRecordDto> records = new ArrayList<>();

        for (ShiftAssignment assignment : assignments) {
            LocalTime scheduledStart = assignment.getStartTime() != null ?
                    LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime();
            LocalTime scheduledEnd = assignment.getEndTime() != null ?
                    LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime();

            boolean isLate = false;
            boolean isEarlyDeparture = false;
            long minutesLate = 0;
            long minutesEarlyDeparture = 0;

            if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
                completedShifts++;
                // Check if late
//                if (assignment.getCheckInTime() != null &&
//                        assignment.getCheckInTime().isAfter(ChronoLocalDateTime.from(scheduledStart.plusMinutes(5)))) {
//                    isLate = true;
//                    lateArrivals++;
//                    minutesLate = ChronoUnit.MINUTES.between(scheduledStart, assignment.getCheckInTime());
//                }
                if (assignment.getCheckInTime() != null) {
                    LocalTime checkInTime = assignment.getCheckInTime().toLocalTime();
                    if (checkInTime.isAfter(scheduledStart.plusMinutes(5))) {
                        isLate = true;
                        lateArrivals++;
                        minutesLate = ChronoUnit.MINUTES.between(scheduledStart, checkInTime);
                    }
                }
                // Check if early departure
//                if (assignment.getCheckOutTime() != null &&
//                        assignment.getCheckOutTime().isBefore(ChronoLocalDateTime.from(scheduledEnd.minusMinutes(5)))) {
//                    isEarlyDeparture = true;
//                    earlyDepartures++;
//                    minutesEarlyDeparture = ChronoUnit.MINUTES.between(assignment.getCheckOutTime(), scheduledEnd);
//                }
                if (assignment.getCheckOutTime() != null) {
                    LocalTime checkOutTime = assignment.getCheckOutTime().toLocalTime();
                    if (checkOutTime.isBefore(scheduledEnd.minusMinutes(5))) {
                        isEarlyDeparture = true;
                        earlyDepartures++;
                        minutesEarlyDeparture = ChronoUnit.MINUTES.between(checkOutTime, scheduledEnd);
                    }
                }
            } else if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.MISSED) {
                missedShifts++;
            }

            // Fix: Add null checks for checkInTime and checkOutTime
            LocalTime actualStartTime = null;
            if (assignment.getCheckInTime() != null) {
                actualStartTime = LocalTime.from(assignment.getCheckInTime());
            }

            LocalTime actualEndTime = null;
            if (assignment.getCheckOutTime() != null) {
                actualEndTime = LocalTime.from(assignment.getCheckOutTime());
            }

            AttendanceRecordDto record = AttendanceRecordDto.builder()
                    .assignmentId(assignment.getId())
                    .shiftId(assignment.getShift().getId())
                    .shiftName(assignment.getShift().getName())
                    .guardId(assignment.getGuard().getId())
                    .guardName(assignment.getGuard().getUser().getFirstName() + " " +
                            assignment.getGuard().getUser().getLastName())
                    .siteId(assignment.getShift().getSite().getId())
                    .siteName(assignment.getShift().getSite().getName())
                    .date(assignment.getAssignmentDate())
                    .scheduledStartTime(scheduledStart)
                    .scheduledEndTime(scheduledEnd)
                    .actualStartTime(actualStartTime)  // Use the null-safe value
                    .actualEndTime(actualEndTime)      // Use the null-safe value
                    .status(assignment.getStatus())
                    .isLate(isLate)
                    .isEarlyDeparture(isEarlyDeparture)
                    .minutesLate(minutesLate)
                    .minutesEarlyDeparture(minutesEarlyDeparture)
                    .build();
            records.add(record);
        }

        double attendanceRate = totalShifts > 0 ?
                (double) completedShifts / totalShifts * 100 : 0;

        String siteName = null;
        if (siteId != null) {
            Site site = siteRepository.findById(siteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
            siteName = site.getName();
        }

        String guardName = null;
        if (guardId != null) {
            Guard guard = guardRepository.findById(guardId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));
            guardName = guard.getUser().getFirstName() + " " + guard.getUser().getLastName();
        }

        return AttendanceReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .siteId(siteId)
                .siteName(siteName)
                .guardId(guardId)
                .guardName(guardName)
                .totalShifts(totalShifts)
                .completedShifts(completedShifts)
                .missedShifts(missedShifts)
                .lateArrivals(lateArrivals)
                .earlyDepartures(earlyDepartures)
                .attendanceRate(attendanceRate)
                .records(records)
                .build();
    }

    // The rest of the methods remain unchanged
    @Transactional
    public IncidentReportDto generateIncidentReport(Long siteId, LocalDate startDate, LocalDate endDate) {
        // Method implementation unchanged
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        List<Incident> incidents;
        if (siteId != null) {
            incidents = incidentRepository.findBySite_IdAndIncidentTimeBetween(
                    siteId, startDateTime, endDateTime);
        } else {
            incidents = incidentRepository.findByIncidentTimeBetween(startDateTime, endDateTime);
        }
        int totalIncidents = incidents.size();
        // Count incidents by type
        Map<String, Integer> incidentsByType = new HashMap<>();
        for (Incident incident : incidents) {
            incidentsByType.put(
                    String.valueOf(incident.getIncidentType()),
                    incidentsByType.getOrDefault(incident.getIncidentType(), 0) + 1);
        }
        // Count incidents by severity
        Map<String, Integer> incidentsBySeverity = new HashMap<>();
        for (Incident incident : incidents) {
            incidentsBySeverity.put(
                    incident.getSeverity().name(),
                    incidentsBySeverity.getOrDefault(incident.getSeverity().name(), 0) + 1);
        }
        List<IncidentSummaryDto> incidentSummaries = incidents.stream()
                .map(incident -> IncidentSummaryDto.builder()
                        .id(incident.getId())
                        .title(incident.getTitle())
                        .incidentType(String.valueOf(incident.getIncidentType()))
                        .incidentTime(incident.getIncidentTime())
                        .siteName(incident.getSite().getName())
                        .guardName(incident.getGuard().getUser().getFirstName() + " " +
                                incident.getGuard().getUser().getLastName())
                        .severity(incident.getSeverity()) // PASS ENUM DIRECTLY
                        .status(incident.getStatus())
                        .build())
                .collect(Collectors.toList());
        String siteName = null;
        if (siteId != null) {
            Site site = siteRepository.findById(siteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
            siteName = site.getName();
        }
        return IncidentReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .siteId(siteId)
                .siteName(siteName)
                .totalIncidents(totalIncidents)
                .incidentsByType(incidentsByType)
                .incidentsBySeverity(incidentsBySeverity)
                .incidents(incidentSummaries)
                .build();
    }

    @Transactional
    public SiteCoverageReportDto generateSiteCoverageReport(Long clientId, LocalDate startDate, LocalDate endDate) {
        // Method implementation unchanged
        List<Site> sites;
        String clientName = null;
        if (clientId != null) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
            sites = siteRepository.findByClient(client);
            clientName = client.getCompanyName();
        } else {
            sites = siteRepository.findAll();
        }
        int totalSites = sites.size();
        int totalShifts = 0;
        int totalHours = 0;
        int coveredHours = 0;
        List<SiteCoverageDetailDto> siteDetails = new ArrayList<>();
        for (Site site : sites) {
            List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShift_Site_IdAndAssignmentDateBetween(
                    site.getId(), startDate, endDate);
            int siteShifts = assignments.size();
            int siteHours = 0;
            int siteCoveredHours = 0;
            for (ShiftAssignment assignment : assignments) {
                LocalTime startTime = assignment.getStartTime() != null ?
                        LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime();
                LocalTime endTime = assignment.getEndTime() != null ?
                        LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime();
                int shiftHours = (int) ChronoUnit.HOURS.between(startTime, endTime);
                if (shiftHours < 0) {
                    // Handle overnight shifts
                    shiftHours += 24;
                }
                siteHours += shiftHours;
                if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
                    siteCoveredHours += shiftHours;
                }
            }
            totalShifts += siteShifts;
            totalHours += siteHours;
            coveredHours += siteCoveredHours;
            double siteCoverageRate = siteHours > 0 ?
                    (double) siteCoveredHours / siteHours * 100 : 0;
            SiteCoverageDetailDto detail = SiteCoverageDetailDto.builder()
                    .siteId(site.getId())
                    .siteName(site.getName())
                    .totalShifts(siteShifts)
                    .totalHours(siteHours)
                    .coveredHours(siteCoveredHours)
                    .coverageRate(siteCoverageRate)
                    .build();
            siteDetails.add(detail);
        }
        double coverageRate = totalHours > 0 ?
                (double) coveredHours / totalHours * 100 : 0;
        return SiteCoverageReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .clientId(clientId)
                .clientName(clientName)
                .totalSites(totalSites)
                .totalShifts(totalShifts)
                .totalHours(totalHours)
                .coveredHours(coveredHours)
                .coverageRate(coverageRate)
                .siteDetails(siteDetails)
                .build();
    }

//    @Transactional
//    public GuardPerformanceReportDto generateGuardPerformanceReport(Long guardId, LocalDate startDate, LocalDate endDate) {
//        // Method implementation unchanged
//        List<Guard> guards;
//        String guardName = null;
//        if (guardId != null) {
//            Guard guard = guardRepository.findById(guardId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + guardId));
//            guards = Collections.singletonList(guard);
//            guardName = guard.getUser().getFirstName() + " " + guard.getUser().getLastName();
//        } else {
//            guards = guardRepository.findAll();
//        }
//        int totalShifts = 0;
//        int completedShifts = 0;
//        int missedShifts = 0;
//        int lateArrivals = 0;
//        int earlyDepartures = 0;
//        int totalIncidents = 0;
//        List<GuardPerformanceDetailDto> performanceByWeek = new ArrayList<>();
//        // Process data by week
//        LocalDate currentWeekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
//        LocalDate currentWeekEnd = currentWeekStart.plusDays(6);
//        while (!currentWeekStart.isAfter(endDate)) {
//            int weekShifts = 0;
//            int weekCompletedShifts = 0;
//            int weekMissedShifts = 0;
//            int weekLateArrivals = 0;
//            int weekIncidents = 0;
//            for (Guard guard : guards) {
//                // Get shifts for this guard in this week
//                List<ShiftAssignment> weekAssignments = shiftAssignmentRepository.findByGuardAndAssignmentDateBetween(
//                        guard, currentWeekStart, currentWeekEnd);
//                weekShifts += weekAssignments.size();
//                for (ShiftAssignment assignment : weekAssignments) {
//                    if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.COMPLETED) {
//                        weekCompletedShifts++;
//                        LocalTime scheduledStart = assignment.getStartTime() != null ?
//                                LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime();
//                        // Check if late
////                        if (assignment.getCheckInTime() != null &&
////                                assignment.getCheckInTime().isAfter(ChronoLocalDateTime.from(scheduledStart.plusMinutes(5)))) {
////                            weekLateArrivals++;
////                        }
//                        if (assignment.getCheckInTime() != null) {
//                            LocalTime checkInTime = assignment.getCheckInTime().toLocalTime();
//                            if (checkInTime.isAfter(scheduledStart.plusMinutes(5))) {
//                                weekLateArrivals++;
//                            }
//                        }
//                    } else if (assignment.getStatus() == ShiftAssignment.AssignmentStatus.MISSED) {
//                        weekMissedShifts++;
//                    }
//                }
//                // Get incidents for this guard in this week
//                LocalDateTime weekStartDateTime = currentWeekStart.atStartOfDay();
//                LocalDateTime weekEndDateTime = currentWeekEnd.plusDays(1).atStartOfDay();
//                List<Incident> weekIncidentsList = incidentRepository.findByGuardAndIncidentTimeBetween(
//                        guard, weekStartDateTime, weekEndDateTime);
//                weekIncidents += weekIncidentsList.size();
//            }
//            totalShifts += weekShifts;
//            completedShifts += weekCompletedShifts;
//            missedShifts += weekMissedShifts;
//            lateArrivals += weekLateArrivals;
//            totalIncidents += weekIncidents;
//            double weekAttendanceRate = weekShifts > 0 ?
//                    (double) weekCompletedShifts / weekShifts * 100 : 0;
//            GuardPerformanceDetailDto weekDetail = GuardPerformanceDetailDto.builder()
//                    .weekStartDate(currentWeekStart)
//                    .weekEndDate(currentWeekEnd)
//                    .totalShifts(weekShifts)
//                    .completedShifts(weekCompletedShifts)
//                    .missedShifts(weekMissedShifts)
//                    .lateArrivals(weekLateArrivals)
//                    .attendanceRate(weekAttendanceRate)
//                    .incidents(weekIncidents)
//                    .build();
//            performanceByWeek.add(weekDetail);
//            // Move to next week
//            currentWeekStart = currentWeekStart.plusWeeks(1);
//            currentWeekEnd = currentWeekEnd.plusWeeks(1);
//        }
//        double attendanceRate = totalShifts > 0 ?
//                (double) completedShifts / totalShifts * 100 : 0;
//        double averageIncidentsPerShift = completedShifts > 0 ?
//                (double) totalIncidents / completedShifts : 0;
//        return GuardPerformanceReportDto.builder()
//                .startDate(startDate)
//                .endDate(endDate)
//                .guardId(guardId)
//                .guardName(guardName)
//                .totalShifts(totalShifts)
//                .completedShifts(completedShifts)
//                .missedShifts(missedShifts)
//                .lateArrivals(lateArrivals)
//                .earlyDepartures(earlyDepartures)
//                .attendanceRate(attendanceRate)
//                .totalIncidents(totalIncidents)
//                .averageIncidentsPerShift(averageIncidentsPerShift)
//                .performanceByWeek(performanceByWeek)
//                .build();
//    }

    @Autowired
    private GuardPerformanceService guardPerformanceService;

    @Transactional
    public GuardPerformanceReportDto generateGuardPerformanceReport(Long guardId, LocalDate startDate, LocalDate endDate) {
        // Delegate to the specialized service
        return guardPerformanceService.generateGuardPerformanceReport(guardId, startDate, endDate);
    }


}
