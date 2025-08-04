package com.dep.soms.repository;

import com.dep.soms.model.Patrol;
import com.dep.soms.model.PatrolAssignment;
import com.dep.soms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatrolAssignmentRepository extends JpaRepository<PatrolAssignment, Long> {
    List<PatrolAssignment> findBySupervisor(User supervisor);

    List<PatrolAssignment> findByPatrolId(Long patrolId);

    List<PatrolAssignment> findBySupervisorAndStatus(User supervisor, PatrolAssignment.PatrolAssignmentStatus status);

    @Query("SELECT pa FROM PatrolAssignment pa WHERE " +
            "pa.patrol.id = :patrolId AND " +
            "pa.supervisor.id = :supervisorId AND " +
            "pa.assignedAt BETWEEN :start AND :end")
    Optional<PatrolAssignment> findByPatrolIdAndSupervisorIdAndAssignedAtBetween(
            @Param("patrolId") Long patrolId,
            @Param("supervisorId") Long supervisorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);


    List<PatrolAssignment> findBySupervisorId(Long supervisorId);

    List<PatrolAssignment> findByStatus(PatrolAssignment.PatrolAssignmentStatus status);
    List<PatrolAssignment> findBySupervisorAndStartTimeBetween(User supervisor, LocalDateTime start, LocalDateTime end);

    // Find assignment by patrol and supervisor
    Optional<PatrolAssignment> findByPatrolIdAndSupervisorId(Long patrolId, Long supervisorId);

    // Find assignments for a supervisor between two dates
    //List<PatrolAssignment> findBySupervisorAndStartTimeBetween(User supervisor, LocalDateTime start, LocalDateTime end);

    // Find assignments for a supervisor after a specific date
    List<PatrolAssignment> findBySupervisorAndStartTimeGreaterThanEqual(User supervisor, LocalDateTime start);

    // Find assignments for a supervisor before a specific date
    List<PatrolAssignment> findBySupervisorAndStartTimeLessThanEqual(User supervisor, LocalDateTime end);

    // Find assignments for a specific site between two dates
    List<PatrolAssignment> findByPatrol_PrimarySite_IdAndStartTimeBetween(Long siteId, LocalDateTime start, LocalDateTime end);

    // Find assignments for a specific site after a specific date
    List<PatrolAssignment> findByPatrol_PrimarySite_IdAndStartTimeGreaterThanEqual(Long siteId, LocalDateTime start);

    // Find assignments for a specific site before a specific date
    List<PatrolAssignment> findByPatrol_PrimarySite_IdAndStartTimeLessThanEqual(Long siteId, LocalDateTime end);

    // Find all assignments between two dates
    List<PatrolAssignment> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<PatrolAssignment> findBySupervisorAndAssignmentDateBetween(User supervisor, LocalDate startDate, LocalDate endDate);
    List<PatrolAssignment> findBySupervisorAndAssignmentDateGreaterThanEqual(User supervisor, LocalDate date);
    List<PatrolAssignment> findBySupervisorAndAssignmentDateLessThanEqual(User supervisor, LocalDate date);
   // List<PatrolAssignment> findBySupervisor(User supervisor);
    List<PatrolAssignment> findByPatrolAndAssignmentDate(Patrol patrol, LocalDate date);
    List<PatrolAssignment> findByPatrol(Patrol patrol);
    Optional<PatrolAssignment> findByPatrolIdAndSupervisorIdAndAssignmentDate(Long patrolId, Long supervisorId, LocalDate assignmentDate);



    List<PatrolAssignment> findByAssignmentDateAndStatusInAndEndTimeBefore(
            LocalDate assignmentDate,
            List<PatrolAssignment.PatrolAssignmentStatus> statuses,
            LocalDateTime endTime
    );

    List<PatrolAssignment> findByAssignmentDateAndStatusAndEndTimeIsNullAndStartTimeBefore(
            LocalDate assignmentDate,
            PatrolAssignment.PatrolAssignmentStatus status,
            LocalDateTime endTime
    );

}