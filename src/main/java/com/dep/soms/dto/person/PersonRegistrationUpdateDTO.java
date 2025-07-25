package com.dep.soms.dto.person;

import com.dep.soms.model.PersonRegistration;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRegistrationUpdateDTO {
    // Personal Information
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String alternatePhone;
    private LocalDate dateOfBirth;
    private PersonRegistration.Gender gender;
    private String nationality;
    private PersonRegistration.MaritalStatus maritalStatus;

    // Physical Address
    private String streetAddress;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String country;

    // Identity Documents
    private String nationalId;
    private String passportNumber;
    private LocalDate passportExpiryDate;
    private String driversLicense;
    private LocalDate driversLicenseExpiry;

    // Physical Characteristics
    private Integer heightCm;
    private Integer weightKg;

    // Languages
    private String primaryLanguage;
    private String otherLanguages;

    // Emergency Contacts
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyPhone;
    private String emergencyContactAddress;

    // Next of Kin
    private String nextOfKinName;
    private String nextOfKinRelationship;
    private String nextOfKinPhone;
    private String nextOfKinAddress;

    // Employment Information
    private String positionAppliedFor;
    private String departmentPreference;
    private String locationPreference;
    private LocalDate availabilityStartDate;
    private BigDecimal expectedSalary;
    private PersonRegistration.WorkAuthorizationStatus workAuthorizationStatus;
    private String workPermitNumber;
    private LocalDate workPermitExpiry;

    // Education & Professional Background
    private String highestEducationLevel;
    private String educationInstitution;
    private Integer graduationYear;
    private String professionalCertifications;
    private String skillsCompetencies;
    private Integer yearsOfExperience;

    // Previous Employment (3 most recent)
    private String previousEmployer1;
    private String previousPosition1;
    private String previousEmploymentDuration1;
    private String previousEmployer2;
    private String previousPosition2;
    private String previousEmploymentDuration2;
    private String previousEmployer3;
    private String previousPosition3;
    private String previousEmploymentDuration3;

    // References
    private String reference1Name;
    private String reference1Position;
    private String reference1Company;
    private String reference1Phone;
    private String reference1Email;
    private String reference2Name;
    private String reference2Position;
    private String reference2Company;
    private String reference2Phone;
    private String reference2Email;

    // Verification Statuses
    private PersonRegistration.VerificationStatus backgroundCheckStatus;
    private LocalDate backgroundCheckDate;
    private String backgroundCheckNotes;
    private PersonRegistration.VerificationStatus medicalCheckStatus;
    private LocalDate medicalCheckDate;
    private LocalDate medicalFitnessValidUntil;
    private PersonRegistration.VerificationStatus drugTestStatus;
    private LocalDate drugTestDate;
    private PersonRegistration.VerificationStatus referenceCheckStatus;
    private LocalDate referenceCheckDate;
    private String referenceCheckNotes;

    // Security Clearance
    private String securityClearanceLevel;
    private LocalDate securityClearanceExpiry;

    // Medical Information
    private String bloodType;
    private String medicalConditions;
    private String allergies;
    private String medications;

    // Registration Status
    private PersonRegistration.RegistrationStatus registrationStatus;
    private String registrationNotes;
}