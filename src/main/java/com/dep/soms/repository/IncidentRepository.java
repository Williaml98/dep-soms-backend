////package com.dep.soms.repository;
////
////import com.dep.soms.dto.incident.IncidentDto;
////import com.dep.soms.model.Guard;
////import com.dep.soms.model.Incident;
////import com.dep.soms.model.Site;
////import com.dep.soms.model.User;
////import org.springframework.data.jpa.repository.JpaRepository;
////import org.springframework.data.jpa.repository.Query;
////import org.springframework.stereotype.Repository;
////
////import java.time.LocalDateTime;
////import java.util.List;
////
////@Repository
////public interface IncidentRepository extends JpaRepository<Incident, Long> {
////    List<Incident> findBySite(Site site);
////    //List<Incident> findByReportedBy(User user);
////    List<Incident> findByStatus(Incident.IncidentStatus status);
////    List<Incident> findBySeverity(Incident.IncidentSeverity severity);
////
////    @Query("SELECT i FROM Incident i WHERE i.incidentTime BETWEEN :startTime AND :endTime")
////    List<Incident> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
////
////    @Query("SELECT i FROM Incident i WHERE i.site.client.id = :clientId")
////    List<Incident> findByClientId(Long clientId);
////
////    List<Incident> findByIncidentTimeBetween(LocalDateTime start, LocalDateTime end);
////    List<Incident> findByIncidentTimeGreaterThanEqual(LocalDateTime start);
////    List<Incident> findByIncidentTimeLessThan(LocalDateTime end);
////
////    List<Incident> findBySiteAndIncidentTimeBetween(Site site, LocalDateTime startTime, LocalDateTime endTime);
////
////    List<Incident> findBySiteAndIncidentTimeGreaterThanEqual(Site site, LocalDateTime startTime);
////
////    List<Incident> findBySiteAndIncidentTimeLessThan(Site site, LocalDateTime endTime);
////
////    List<Incident> findByGuard(Guard guard);
////
////    List<Incident> findByGuardAndIncidentTimeLessThan(Guard guard, LocalDateTime endDateTime);
////
////    List<Incident> findByGuardAndIncidentTimeGreaterThanEqual(Guard guard, LocalDateTime startDateTime);
////
////    List<Incident> findByGuardAndIncidentTimeBetween(Guard guard, LocalDateTime startDateTime, LocalDateTime endDateTime);
////
////    List<Incident> findBySite_IdAndIncidentTimeBetween(Long siteId, LocalDateTime startDateTime, LocalDateTime endDateTime);
////
////    //List<Incident> findByReportedByUser(User user);
////    List<IncidentDto> getIncidentsByReportedByUser(User user);
////
////
////}
//
//package com.dep.soms.repository;
//
//import com.dep.soms.dto.incident.IncidentDto;
//import com.dep.soms.model.Guard;
//import com.dep.soms.model.Incident;
//import com.dep.soms.model.Site;
//import com.dep.soms.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface IncidentRepository extends JpaRepository<Incident, Long> {
//    List<Incident> findBySite(Site site);
//
//    //List<Incident> findByReportedBy(User user);
//
//    List<Incident> findByStatus(Incident.IncidentStatus status);
//
//    List<Incident> findBySeverity(Incident.IncidentSeverity severity);
//
//    @Query("SELECT i FROM Incident i WHERE i.incidentTime BETWEEN :startTime AND :endTime")
//    List<Incident> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
//
//    @Query("SELECT i FROM Incident i WHERE i.site.client.id = :clientId")
//    List<Incident> findByClientId(Long clientId);
//
//    List<Incident> findByIncidentTimeBetween(LocalDateTime start, LocalDateTime end);
//
//    List<Incident> findByIncidentTimeGreaterThanEqual(LocalDateTime start);
//
//    List<Incident> findByIncidentTimeLessThan(LocalDateTime end);
//
//    List<Incident> findBySiteAndIncidentTimeBetween(Site site, LocalDateTime startTime, LocalDateTime endTime);
//
//    List<Incident> findBySiteAndIncidentTimeGreaterThanEqual(Site site, LocalDateTime startTime);
//
//    List<Incident> findBySiteAndIncidentTimeLessThan(Site site, LocalDateTime endTime);
//
//    List<Incident> findByGuard(Guard guard);
//
//    List<Incident> findByGuardAndIncidentTimeLessThan(Guard guard, LocalDateTime endDateTime);
//
//    List<Incident> findByGuardAndIncidentTimeGreaterThanEqual(Guard guard, LocalDateTime startDateTime);
//
//    List<Incident> findByGuardAndIncidentTimeBetween(Guard guard, LocalDateTime startDateTime, LocalDateTime endDateTime);
//
//    List<Incident> findBySite_IdAndIncidentTimeBetween(Long siteId, LocalDateTime startDateTime, LocalDateTime endDateTime);
//
//    // Add this method - note that it returns List<Incident>, not List<IncidentDto>
//    List<Incident> findByReportedByUser(User user);
//}

package com.dep.soms.repository;

import com.dep.soms.model.Guard;
import com.dep.soms.model.Incident;
import com.dep.soms.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Find incidents by guard
    List<Incident> findByGuardOrderByIncidentTimeDesc(Guard guard);
    List<Incident> findByGuard(Guard guard);
    List<Incident> findByGuardAndIncidentTimeBetween(Guard guard, LocalDateTime startTime, LocalDateTime endTime);
    List<Incident> findByGuardAndIncidentTimeGreaterThanEqual(Guard guard, LocalDateTime startTime);
    List<Incident> findByGuardAndIncidentTimeLessThan(Guard guard, LocalDateTime endTime);

    // Find incidents by site
    List<Incident> findBySite(Site site);
    List<Incident> findBySiteOrderByIncidentTimeDesc(Site site);
    List<Incident> findBySiteAndIncidentTimeBetween(Site site, LocalDateTime startTime, LocalDateTime endTime);
    List<Incident> findBySiteAndIncidentTimeGreaterThanEqual(Site site, LocalDateTime startTime);
    List<Incident> findBySiteAndIncidentTimeLessThan(Site site, LocalDateTime endTime);

    // Find incidents by time range
    List<Incident> findByIncidentTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<Incident> findByIncidentTimeGreaterThanEqual(LocalDateTime startTime);
    List<Incident> findByIncidentTimeLessThan(LocalDateTime endTime);

    // Find incidents by status
    List<Incident> findByStatus(Incident.IncidentStatus status);
    List<Incident> findBySiteAndStatus(Site site, Incident.IncidentStatus status);

    // Find incidents by severity
    List<Incident> findBySeverity(Incident.IncidentSeverity severity);
    List<Incident> findBySiteAndSeverity(Site site, Incident.IncidentSeverity severity);

    // Find incidents by type
    List<Incident> findByIncidentType(Incident.IncidentType incidentType);
    List<Incident> findBySiteAndIncidentType(Site site, Incident.IncidentType incidentType);

    // Complex queries
    @Query("SELECT i FROM Incident i WHERE i.site = :site AND i.incidentTime BETWEEN :startTime AND :endTime ORDER BY i.incidentTime DESC")
    List<Incident> findBySiteAndTimeRangeOrderByTimeDesc(
            @Param("site") Site site,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT i FROM Incident i WHERE i.guard = :guard AND i.incidentTime BETWEEN :startTime AND :endTime ORDER BY i.incidentTime DESC")
    List<Incident> findByGuardAndTimeRangeOrderByTimeDesc(
            @Param("guard") Guard guard,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Count queries for statistics
    long countBySite(Site site);
    long countByGuard(Guard guard);
    long countBySiteAndStatus(Site site, Incident.IncidentStatus status);
    long countByGuardAndStatus(Guard guard, Incident.IncidentStatus status);
    long countBySiteAndIncidentTimeBetween(Site site, LocalDateTime startTime, LocalDateTime endTime);
    long countByGuardAndIncidentTimeBetween(Guard guard, LocalDateTime startTime, LocalDateTime endTime);

    List<Incident> findBySite_IdAndIncidentTimeBetween(Long siteId, LocalDateTime startDateTime, LocalDateTime endDateTime);
   // List<Incident> findBySiteAndIncidentTimeBetween(Site site, LocalDateTime startTime, LocalDateTime endTime);

}
