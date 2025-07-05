package com.dep.soms.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "person_registrations")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PersonRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // Personal Information
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "alternate_phone")
    private String alternatePhone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "nationality")
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    // Physical Address
    @Column(name = "street_address", length = 500)
    private String streetAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "state_province")
    private String stateProvince;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    // Identity Documents
    @Column(name = "national_id", unique = true)
    private String nationalId;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_expiry_date")
    private LocalDate passportExpiryDate;

    @Column(name = "drivers_license")
    private String driversLicense;

    @Column(name = "drivers_license_expiry")
    private LocalDate driversLicenseExpiry;

    // Physical Characteristics (for security roles)
    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Integer weightKg;

    // Languages
    @Column(name = "primary_language")
    private String primaryLanguage;

    @Column(name = "other_languages", length = 500)
    private String otherLanguages; // Comma separated or JSON

    // Emergency Contacts
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;

    @Column(name = "emergency_phone")
    private String emergencyPhone;

    @Column(name = "emergency_contact_address", length = 500)
    private String emergencyContactAddress;

    // Next of Kin (different from emergency contact)
    @Column(name = "next_of_kin_name")
    private String nextOfKinName;

    @Column(name = "next_of_kin_relationship")
    private String nextOfKinRelationship;

    @Column(name = "next_of_kin_phone")
    private String nextOfKinPhone;

    @Column(name = "next_of_kin_address", length = 500)
    private String nextOfKinAddress;

    // Employment Information
    @Column(name = "position_applied_for")
    private String positionAppliedFor;

    @Column(name = "department_preference")
    private String departmentPreference;

    @Column(name = "location_preference")
    private String locationPreference;

    @Column(name = "availability_start_date")
    private LocalDate availabilityStartDate;

    @Column(name = "expected_salary", precision = 10, scale = 2)
    private BigDecimal expectedSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_authorization_status")
    private WorkAuthorizationStatus workAuthorizationStatus;

    @Column(name = "work_permit_number")
    private String workPermitNumber;

    @Column(name = "work_permit_expiry")
    private LocalDate workPermitExpiry;

    // Education & Professional Background
    @Column(name = "highest_education_level")
    private String highestEducationLevel;

    @Column(name = "education_institution")
    private String educationInstitution;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "professional_certifications", length = 1000)
    private String professionalCertifications;

    @Column(name = "skills_competencies", length = 1000)
    private String skillsCompetencies;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "previous_employer_1")
    private String previousEmployer1;

    @Column(name = "previous_position_1")
    private String previousPosition1;

    @Column(name = "previous_employment_duration_1")
    private String previousEmploymentDuration1;

    @Column(name = "previous_employer_2")
    private String previousEmployer2;

    @Column(name = "previous_position_2")
    private String previousPosition2;

    @Column(name = "previous_employment_duration_2")
    private String previousEmploymentDuration2;

    @Column(name = "previous_employer_3")
    private String previousEmployer3;

    @Column(name = "previous_position_3")
    private String previousPosition3;

    @Column(name = "previous_employment_duration_3")
    private String previousEmploymentDuration3;

    // References
    @Column(name = "reference_1_name")
    private String reference1Name;

    @Column(name = "reference_1_position")
    private String reference1Position;

    @Column(name = "reference_1_company")
    private String reference1Company;

    @Column(name = "reference_1_phone")
    private String reference1Phone;

    @Column(name = "reference_1_email")
    private String reference1Email;

    @Column(name = "reference_2_name")
    private String reference2Name;

    @Column(name = "reference_2_position")
    private String reference2Position;

    @Column(name = "reference_2_company")
    private String reference2Company;

    @Column(name = "reference_2_phone")
    private String reference2Phone;

    @Column(name = "reference_2_email")
    private String reference2Email;

    // Background Verification Status
    @Enumerated(EnumType.STRING)
    @Column(name = "background_check_status")
    private VerificationStatus backgroundCheckStatus;

    @Column(name = "background_check_date")
    private LocalDate backgroundCheckDate;

    @Column(name = "background_check_notes", length = 1000)
    private String backgroundCheckNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "medical_check_status")
    private VerificationStatus medicalCheckStatus;

    @Column(name = "medical_check_date")
    private LocalDate medicalCheckDate;

    @Column(name = "medical_fitness_valid_until")
    private LocalDate medicalFitnessValidUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "drug_test_status")
    private VerificationStatus drugTestStatus;

    @Column(name = "drug_test_date")
    private LocalDate drugTestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_check_status")
    private VerificationStatus referenceCheckStatus;

    @Column(name = "reference_check_date")
    private LocalDate referenceCheckDate;

    @Column(name = "reference_check_notes", length = 1000)
    private String referenceCheckNotes;

    // Security Clearance
    @Column(name = "security_clearance_level")
    private String securityClearanceLevel;

    @Column(name = "security_clearance_expiry")
    private LocalDate securityClearanceExpiry;

    // Consent and Legal
    @Column(name = "background_check_consent")
    private Boolean backgroundCheckConsent;

    @Column(name = "data_processing_consent")
    private Boolean dataProcessingConsent;

    @Column(name = "terms_conditions_accepted")
    private Boolean termsConditionsAccepted;

    @Column(name = "privacy_policy_accepted")
    private Boolean privacyPolicyAccepted;

    @Column(name = "consent_date")
    private LocalDateTime consentDate;

    // Medical Information
    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "medical_conditions", length = 1000)
    private String medicalConditions;

    @Column(name = "allergies", length = 500)
    private String allergies;

    @Column(name = "medications", length = 500)
    private String medications;

    // File Uploads (as strings)
    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "resume_cv")
    private String resumeCv;

    @Column(name = "national_id_copy")
    private String nationalIdCopy;

    @Column(name = "passport_copy")
    private String passportCopy;

    @Column(name = "drivers_license_copy")
    private String driversLicenseCopy;

    @Column(name = "education_certificates")
    private String educationCertificates;

    @Column(name = "professional_certificates")
    private String professionalCertificatesFiles;

    @Column(name = "medical_certificate")
    private String medicalCertificate;

    @Column(name = "reference_letters")
    private String referenceLetters;

    @Column(name = "other_documents")
    private String otherDocuments;

    // Original fields
    @Column(name = "registration_notes", length = 1000)
    private String registrationNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
    private RegistrationStatus registrationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by_user_id")
    private User registeredBy;

    @OneToOne(mappedBy = "personRegistration", cascade = CascadeType.ALL)
    private TrainingRecord trainingRecord;

    @OneToOne(mappedBy = "personRegistration", cascade = CascadeType.ALL)
    private Guard guard;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum MaritalStatus {
        SINGLE, MARRIED, DIVORCED, WIDOWED, SEPARATED, OTHER
    }

    public enum WorkAuthorizationStatus {
        CITIZEN, PERMANENT_RESIDENT, WORK_PERMIT, STUDENT_VISA, PENDING_AUTHORIZATION, NOT_AUTHORIZED
    }

    public enum VerificationStatus {
        PENDING, IN_PROGRESS, COMPLETED_PASS, COMPLETED_FAIL, NOT_REQUIRED, EXPIRED
    }

    public enum RegistrationStatus {
        PENDING_REVIEW,           // Stage 1: Just registered
        APPROVED_FOR_TRAINING,    // Stage 1: Approved, ready for training
        IN_TRAINING,             // Stage 2: Currently in training
        TRAINING_COMPLETED,      // Stage 2: Finished training
        TRAINING_FAILED,         // Stage 2: Failed training
        APPROVED_FOR_GUARD,      // Stage 2: Passed training, ready for guard registration
        ACTIVE_GUARD,            // Stage 3: Fully registered guard
        REJECTED,                // Rejected at any stage
        INACTIVE                 // Deactivated
    }
}