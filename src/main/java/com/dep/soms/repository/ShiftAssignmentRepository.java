//package com.dep.soms.repository;
//
//import com.dep.soms.model.Guard;
//import com.dep.soms.model.Shift;
//import com.dep.soms.model.ShiftAssignment;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Repository
//public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
//    List<ShiftAssignment> findByShift(Shift shift);
//    List<ShiftAssignment> findByGuard(Guard guard);
//    List<ShiftAssignment> findByAssignmentDate(LocalDate date);
//    List<ShiftAssignment> findByGuardAndAssignmentDate(Guard guard, LocalDate date);
//
//    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.shift.site.id = :siteId AND sa.assignmentDate = :date")
//    List<ShiftAssignment> findBySiteAndDate(Long siteId, LocalDate date);
//
//    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.assignmentDate BETWEEN :startDate AND :endDate")
//    List<ShiftAssignment> findByDateRange(LocalDate startDate, LocalDate endDate);
//
//    List<ShiftAssignment> findByShift_Site_IdAndGuard_IdAndDateBetween(Long siteId, Long guardId, LocalDate startDate, LocalDate endDate);
//
//    List<ShiftAssignment> findByShift_Site_IdAndDateBetween(Long siteId, LocalDate startDate, LocalDate endDate);
//
//    List<ShiftAssignment> findByGuard_IdAndDateBetween(Long guardId, LocalDate startDate, LocalDate endDate);
//
//    List<ShiftAssignment> findByDateBetween(LocalDate startDate, LocalDate endDate);
//
//    List<ShiftAssignment> findByGuardAndDateBetween(Guard guard, LocalDate currentWeekStart, LocalDate currentWeekEnd);
//
//    List<ShiftAssignment> findByGuardAndDateGreaterThanEqual(Guard guard, LocalDate startDate);
//
//    List<ShiftAssignment> findByGuardAndDateLessThanEqual(Guard guard, LocalDate endDate);
//
//    List<ShiftAssignment> findByShiftAndDate(Shift shift, LocalDate date);
//
//    List<ShiftAssignment> findByShift_Site_IdAndDate(Long siteId, LocalDate date);
//
//    List<ShiftAssignment> findByShift_Site_Id(Long siteId);
//
//    List<ShiftAssignment> findByGuardAndDate(Guard guard, LocalDate date);
//
//    List<ShiftAssignment> findByGuardAndDateAndIdNot(Guard guard, LocalDate date, Long id);
//
//    // List<Guard> findByGuardAndDate(Guard guard, LocalDate date);
//}

package com.dep.soms.repository;

import com.dep.soms.model.Guard;
import com.dep.soms.model.Shift;
import com.dep.soms.model.ShiftAssignment;
import com.dep.soms.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    List<ShiftAssignment> findByShift(Shift shift);

    List<ShiftAssignment> findByGuard(Guard guard);

    List<ShiftAssignment> findByAssignmentDate(LocalDate date);

   // List<ShiftAssignment> findByGuardAndAssignmentDate(Guard guard, LocalDate date);

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.shift.site.id = :siteId AND sa.assignmentDate = :date")
    List<ShiftAssignment> findBySiteAndDate(Long siteId, LocalDate date);

    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.assignmentDate BETWEEN :startDate AND :endDate")
    List<ShiftAssignment> findByDateRange(LocalDate startDate, LocalDate endDate);

    List<ShiftAssignment> findByShift_Site_IdAndGuard_IdAndAssignmentDateBetween(Long siteId, Long guardId, LocalDate startDate, LocalDate endDate);

    List<ShiftAssignment> findByShift_Site_IdAndAssignmentDateBetween(Long siteId, LocalDate startDate, LocalDate endDate);

    List<ShiftAssignment> findByGuard_IdAndAssignmentDateBetween(Long guardId, LocalDate startDate, LocalDate endDate);

    List<ShiftAssignment> findByAssignmentDateBetween(LocalDate startDate, LocalDate endDate);

    List<ShiftAssignment> findByGuardAndAssignmentDateBetween(Guard guard, LocalDate currentWeekStart, LocalDate currentWeekEnd);

    List<ShiftAssignment> findByGuardAndAssignmentDateGreaterThanEqual(Guard guard, LocalDate startDate);

    List<ShiftAssignment> findByGuardAndAssignmentDateLessThanEqual(Guard guard, LocalDate endDate);

    List<ShiftAssignment> findByShiftAndAssignmentDate(Shift shift, LocalDate date);

    List<ShiftAssignment> findByShift_Site_IdAndAssignmentDate(Long siteId, LocalDate date);

    List<ShiftAssignment> findByShift_Site_Id(Long siteId);

    List<ShiftAssignment> findByGuardAndAssignmentDateAndIdNot(Guard guard, LocalDate date, Long id);

    // Commented out as it appears to be a duplicate or note
    // List<Guard> findByGuardAndDate(Guard guard, LocalDate date);
    // Add this method to your existing repository interface
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.guard = :guard AND sa.assignmentDate = :date")
    List<ShiftAssignment> findByGuardAndAssignmentDate(@Param("guard") Guard guard, @Param("date") LocalDate date);

    boolean existsByShiftIdAndGuardIdAndAssignmentDate(Long shiftId, Long guardId, LocalDate assignmentDate);

    /**
     * Find assignments by date range for conflict checking
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.guard.id = :guardId AND sa.assignmentDate BETWEEN :startDate AND :endDate")
    List<ShiftAssignment> findByGuardIdAndDateRange(@Param("guardId") Long guardId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // Add this method
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.shift.id = :shiftId AND sa.guard.id = :guardId AND sa.assignmentDate = :assignmentDate")
    Optional<ShiftAssignment> findByShiftIdAndGuardIdAndAssignmentDate(@Param("shiftId") Long shiftId,
                                                                       @Param("guardId") Long guardId,
                                                                       @Param("assignmentDate") LocalDate assignmentDate);

    // Add this for the scheduled task
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.status = :status AND sa.endTime < :endTime")
    List<ShiftAssignment> findByStatusAndEndTimeBefore(@Param("status") ShiftAssignment.AssignmentStatus status,
                                                       @Param("endTime") LocalDateTime endTime);

    // Add this method to find current active shift for a guard
    @Query("SELECT sa FROM ShiftAssignment sa " +
            "JOIN sa.shift s " +
            "WHERE sa.guard = :guard " +
            "AND sa.status = 'ACTIVE' " +
            "AND s.startTime <= :currentTime " +
            "AND s.endTime >= :currentTime " +
            "ORDER BY s.startTime DESC")
    Optional<ShiftAssignment> findCurrentActiveShiftByGuard(
            @Param("guard") Guard guard,
            @Param("currentTime") LocalDateTime currentTime);

    List<ShiftAssignment> findByShiftId(Long shiftId);

    // New method for finding current active shift
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.guard = :guard AND sa.status = 'IN_PROGRESS' AND sa.assignmentDate = CURRENT_DATE")
    Optional<ShiftAssignment> findCurrentActiveShiftByGuard(@Param("guard") Guard guard);

    // Alternative method if you want to check by time as well
    @Query("SELECT sa FROM ShiftAssignment sa WHERE sa.guard = :guard AND sa.status = 'IN_PROGRESS' AND sa.checkInTime IS NOT NULL AND sa.checkOutTime IS NULL")
    Optional<ShiftAssignment> findCurrentInProgressShiftByGuard(@Param("guard") Guard guard);

    List<ShiftAssignment> findByShift_Site_IdAndAssignmentDateGreaterThanEqual(Long siteId, LocalDate startDate);

    List<ShiftAssignment> findByShift_Site_IdAndAssignmentDateLessThanEqual(Long siteId, LocalDate endDate);


    List<ShiftAssignment> findByAssignmentDateAndCheckInTimeIsNullAndEndTimeBefore(
            LocalDate assignmentDate, LocalDateTime endTime);

    List<ShiftAssignment> findByAssignmentDateAndCheckInTimeIsNotNullAndCheckOutTimeIsNullAndEndTimeBefore(
            LocalDate assignmentDate, LocalDateTime endTime);

    // Add these methods to your ShiftAssignmentRepository

    List<ShiftAssignment> findByStartTimeBetweenAndCheckInTimeIsNull(
            LocalDateTime startAfter, LocalDateTime startBefore);

    @Query("SELECT DISTINCT s.shift.site FROM ShiftAssignment s WHERE s.assignmentDate = :date")
    List<Site> findDistinctSitesByAssignmentDate(@Param("date") LocalDate date);


    List<ShiftAssignment> findByShift_Site_IdInAndAssignmentDate(List<Long> siteIds, LocalDate assignmentDate);


}
