package com.dep.soms.repository;

import com.dep.soms.model.PersonRegistration;
import com.dep.soms.model.PersonRegistration.RegistrationStatus;
import com.dep.soms.model.PersonRegistration.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRegistrationRepository extends JpaRepository<PersonRegistration, Long> {

    // Basic queries by status
    List<PersonRegistration> findByRegistrationStatus(RegistrationStatus status);
    Page<PersonRegistration> findByRegistrationStatus(RegistrationStatus status, Pageable pageable);

    // Find by email (for uniqueness check)
    Optional<PersonRegistration> findByEmail(String email);
    boolean existsByEmail(String email);

    // Find by national ID (for uniqueness check)
    Optional<PersonRegistration> findByNationalId(String nationalId);
    boolean existsByNationalId(String nationalId);

    // Search functionality
    @Query("SELECT p FROM PersonRegistration p " +
            "WHERE (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.nationalId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PersonRegistration> searchPersonRegistrations(@Param("search") String search, Pageable pageable);

    // Search with status filter
    @Query("SELECT p FROM PersonRegistration p " +
            "WHERE p.registrationStatus = :status " +
            "AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.nationalId) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PersonRegistration> searchByStatusAndKeyword(@Param("status") RegistrationStatus status,
                                                      @Param("search") String search,
                                                      Pageable pageable);

    // Admin queries - get persons ready for different stages
    @Query("SELECT p FROM PersonRegistration p " +
            "LEFT JOIN FETCH p.trainingRecord tr " +
            "LEFT JOIN FETCH tr.attendances " +
            "LEFT JOIN FETCH tr.certifications " +
            "WHERE p.registrationStatus = :status")
    List<PersonRegistration> findByRegistrationStatusWithTrainingDetails(@Param("status") RegistrationStatus status);

    // Get persons with complete details (for admin review)
    @Query("SELECT p FROM PersonRegistration p " +
            "LEFT JOIN FETCH p.registeredBy " +
            "LEFT JOIN FETCH p.trainingRecord " +
            "LEFT JOIN FETCH p.guard " +
            "LEFT JOIN FETCH p.user " +
            "WHERE p.id = :id")
    Optional<PersonRegistration> findByIdWithAllDetails(@Param("id") Long id);

    // Statistics queries
    long countByRegistrationStatus(RegistrationStatus status);

    @Query("SELECT COUNT(p) FROM PersonRegistration p WHERE p.createdAt >= :startDate")
    long countRegistrationsAfterDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(p) FROM PersonRegistration p " +
            "WHERE p.registrationStatus = :status AND p.createdAt >= :startDate")
    long countByStatusAfterDate(@Param("status") RegistrationStatus status,
                                @Param("startDate") LocalDateTime startDate);

    // Verification status queries
    List<PersonRegistration> findByBackgroundCheckStatus(VerificationStatus status);
    List<PersonRegistration> findByMedicalCheckStatus(VerificationStatus status);
    List<PersonRegistration> findByDrugTestStatus(VerificationStatus status);
    List<PersonRegistration> findByReferenceCheckStatus(VerificationStatus status);

    // Find persons with expiring documents
    @Query("SELECT p FROM PersonRegistration p " +
            "WHERE p.passportExpiryDate BETWEEN :startDate AND :endDate " +
            "OR p.driversLicenseExpiry BETWEEN :startDate AND :endDate " +
            "OR p.workPermitExpiry BETWEEN :startDate AND :endDate " +
            "OR p.medicalFitnessValidUntil BETWEEN :startDate AND :endDate " +
            "OR p.securityClearanceExpiry BETWEEN :startDate AND :endDate")
    List<PersonRegistration> findPersonsWithExpiringDocuments(@Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    // Find by registered by user
    List<PersonRegistration> findByRegisteredBy_Id(Long userId);
    Page<PersonRegistration> findByRegisteredBy_Id(Long userId, Pageable pageable);

    // Find recent registrations
    @Query("SELECT p FROM PersonRegistration p " +
            "WHERE p.createdAt >= :since " +
            "ORDER BY p.createdAt DESC")
    List<PersonRegistration> findRecentRegistrations(@Param("since") LocalDateTime since);

    // Dashboard queries
    @Query("SELECT p.registrationStatus, COUNT(p) FROM PersonRegistration p GROUP BY p.registrationStatus")
    List<Object[]> getRegistrationStatusCounts();

    @Query("SELECT p.backgroundCheckStatus, COUNT(p) FROM PersonRegistration p GROUP BY p.backgroundCheckStatus")
    List<Object[]> getBackgroundCheckStatusCounts();

    // Find persons by multiple statuses
    List<PersonRegistration> findByRegistrationStatusIn(List<RegistrationStatus> statuses);

    // Custom query for complex filtering
    @Query("SELECT p FROM PersonRegistration p " +
            "WHERE (:status IS NULL OR p.registrationStatus = :status) " +
            "AND (:nationality IS NULL OR LOWER(p.nationality) = LOWER(:nationality)) " +
            "AND (:positionAppliedFor IS NULL OR LOWER(p.positionAppliedFor) LIKE LOWER(CONCAT('%', :positionAppliedFor, '%'))) " +
            "AND (:fromDate IS NULL OR p.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR p.createdAt <= :toDate)")
    Page<PersonRegistration> findWithFilters(@Param("status") RegistrationStatus status,
                                             @Param("nationality") String nationality,
                                             @Param("positionAppliedFor") String positionAppliedFor,
                                             @Param("fromDate") LocalDateTime fromDate,
                                             @Param("toDate") LocalDateTime toDate,
                                             Pageable pageable);

    List<PersonRegistration> findByRegistrationStatusAndGuardIsNull(PersonRegistration.RegistrationStatus status);
}
