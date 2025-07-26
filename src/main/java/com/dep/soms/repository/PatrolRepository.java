package com.dep.soms.repository;

import com.dep.soms.model.Patrol;
import com.dep.soms.model.Patrol.PatrolStatus;
import com.dep.soms.model.Site;
import com.dep.soms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

//@Repository
//public interface PatrolRepository extends JpaRepository<Patrol, Long> {
//    List<Patrol> findBySupervisor(User supervisor);
//    List<Patrol> findBySupervisorAndStatus(User supervisor, PatrolStatus status);
//    List<Patrol> findByStatus(PatrolStatus status);
//    List<Patrol> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
//    //List<Patrol> findBySiteId(Long siteId);
//    List<Patrol> findByStatusAndEndTimeBefore(PatrolStatus status, LocalDateTime endTime);
//    List<Patrol> findByStartTimeBetweenOrEndTimeBetween(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2);
//    List<Patrol> findBySite(Site site);
//
//    List<Patrol> findBySites_Id(Long siteId);
//
//@Repository
//public interface PatrolRepository extends JpaRepository<Patrol, Long> {
//    List<Patrol> findBySupervisor(User supervisor);
//    List<Patrol> findBySupervisorAndStatus(User supervisor, PatrolStatus status);
//    List<Patrol> findByStatus(PatrolStatus status);
//    List<Patrol> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
//    List<Patrol> findByStatusAndEndTimeBefore(PatrolStatus status, LocalDateTime endTime);
//    List<Patrol> findByStartTimeBetweenOrEndTimeBetween(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2);
//    List<Patrol> findBySites_Id(Long siteId); // Keep this correct version


@Repository
public interface PatrolRepository extends JpaRepository<Patrol, Long> {
    List<Patrol> findByStatus(PatrolStatus status);
    List<Patrol> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Patrol> findByStatusAndEndTimeBefore(PatrolStatus status, LocalDateTime endTime);
    List<Patrol> findByStartTimeBetweenOrEndTimeBetween(
            LocalDateTime start1, LocalDateTime end1,
            LocalDateTime start2, LocalDateTime end2);
    List<Patrol> findBySites_Id(Long siteId);


}