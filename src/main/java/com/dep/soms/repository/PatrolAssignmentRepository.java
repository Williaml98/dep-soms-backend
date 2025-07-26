package com.dep.soms.repository;

import com.dep.soms.model.PatrolAssignment;
import com.dep.soms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}