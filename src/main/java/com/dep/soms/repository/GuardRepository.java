package com.dep.soms.repository;

import com.dep.soms.model.Guard;
import com.dep.soms.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuardRepository extends JpaRepository<Guard, Long> {
    Optional<Guard> findByUser(User user);
    Optional<Guard> findByBadgeNumber(String badgeNumber);
    List<Guard> findByStatus(Guard.GuardStatus status);
    Optional<Guard> findByUserId(Long userId);

    @Query("SELECT g FROM Guard g WHERE g.licenseExpiry <= :expiryDate")
    List<Guard> findByLicenseExpiryBefore(LocalDate expiryDate);

    @Query("SELECT g FROM Guard g JOIN g.certifications c WHERE c.expiryDate <= :expiryDate")
    List<Guard> findByCertificationExpiryBefore(LocalDate expiryDate);

    List<Guard> findByUser_ActiveTrue();


    /**
     * Check if guard exists by user ID
     */
    boolean existsByUserId(Long userId);

    @Query("""
  SELECT DISTINCT sa.guard
  FROM ShiftAssignment sa
  JOIN sa.shift s
  JOIN s.site site
  JOIN site.client client
  WHERE client.id = :clientId
""")
    List<Guard> findGuardsByClientId(@Param("clientId") Long clientId);

    Optional<Guard> findTopByOrderByIdDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT MAX(CAST(SUBSTRING(g.badgeNumber, 5) AS int)) FROM Guard g WHERE g.badgeNumber LIKE 'DEP-%'")
    Optional<Integer> findMaxBadgeNumber();

}
