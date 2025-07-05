package com.dep.soms.service;

import com.dep.soms.dto.person.*;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.PersonRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.reflections.Reflections.log;

@Service
public class PersonRegistrationService {

    private final PersonRegistrationRepository personRegistrationRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;


    @Transactional
    public PersonRegistrationResponseDTO createRegistration(
            PersonRegistrationRequestDTO registrationDTO,
            MultipartFile resumeCv,
            MultipartFile nationalIdCopy,
            MultipartFile passportCopy,
            MultipartFile driversLicenseCopy,
            MultipartFile educationCertificates,
            MultipartFile professionalCertificatesFiles,
            MultipartFile medicalCertificate,
            MultipartFile referenceLetters,
            MultipartFile otherDocuments) throws IOException {

        // Validate email and national ID uniqueness
        if (personRegistrationRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (registrationDTO.getNationalId() != null &&
                personRegistrationRepository.existsByNationalId(registrationDTO.getNationalId())) {
            throw new IllegalArgumentException("National ID already exists");
        }

        // Map DTO to entity
        PersonRegistration registration = new PersonRegistration();
        registration.setFirstName(registrationDTO.getFirstName());
        registration.setMiddleName(registrationDTO.getMiddleName());
        registration.setLastName(registrationDTO.getLastName());
        registration.setEmail(registrationDTO.getEmail());
        registration.setPhoneNumber(registrationDTO.getPhoneNumber());
        registration.setAlternatePhone(registrationDTO.getAlternatePhone());
        registration.setDateOfBirth(registrationDTO.getDateOfBirth());
        registration.setGender(PersonRegistration.Gender.valueOf(registrationDTO.getGender()));
        registration.setNationality(registrationDTO.getNationality());
        registration.setMaritalStatus(PersonRegistration.MaritalStatus.valueOf(registrationDTO.getMaritalStatus()));
        registration.setStreetAddress(registrationDTO.getStreetAddress());
        registration.setCity(registrationDTO.getCity());
        registration.setStateProvince(registrationDTO.getStateProvince());
        registration.setPostalCode(registrationDTO.getPostalCode());
        registration.setCountry(registrationDTO.getCountry());
        registration.setNationalId(registrationDTO.getNationalId());
        registration.setPassportNumber(registrationDTO.getPassportNumber());
        registration.setPassportExpiryDate(registrationDTO.getPassportExpiryDate());
        registration.setDriversLicense(registrationDTO.getDriversLicense());
        registration.setDriversLicenseExpiry(registrationDTO.getDriversLicenseExpiry());
        registration.setHeightCm(registrationDTO.getHeightCm());
        registration.setWeightKg(registrationDTO.getWeightKg());
        registration.setPrimaryLanguage(registrationDTO.getPrimaryLanguage());
        registration.setOtherLanguages(registrationDTO.getOtherLanguages());
        registration.setEmergencyContactName(registrationDTO.getEmergencyContactName());
        registration.setEmergencyContactRelationship(registrationDTO.getEmergencyContactRelationship());
        registration.setEmergencyPhone(registrationDTO.getEmergencyPhone());
        registration.setEmergencyContactAddress(registrationDTO.getEmergencyContactAddress());
        registration.setNextOfKinName(registrationDTO.getNextOfKinName());
        registration.setNextOfKinRelationship(registrationDTO.getNextOfKinRelationship());
        registration.setNextOfKinPhone(registrationDTO.getNextOfKinPhone());
        registration.setNextOfKinAddress(registrationDTO.getNextOfKinAddress());
        registration.setPositionAppliedFor(registrationDTO.getPositionAppliedFor());
        registration.setDepartmentPreference(registrationDTO.getDepartmentPreference());
        registration.setLocationPreference(registrationDTO.getLocationPreference());
        registration.setAvailabilityStartDate(registrationDTO.getAvailabilityStartDate());
        registration.setExpectedSalary(registrationDTO.getExpectedSalary() != null ?
                BigDecimal.valueOf(registrationDTO.getExpectedSalary()) : null);
        registration.setWorkAuthorizationStatus(PersonRegistration.WorkAuthorizationStatus.valueOf(registrationDTO.getWorkAuthorizationStatus()));
        registration.setWorkPermitNumber(registrationDTO.getWorkPermitNumber());
        registration.setWorkPermitExpiry(registrationDTO.getWorkPermitExpiry());
        registration.setHighestEducationLevel(registrationDTO.getHighestEducationLevel());
        registration.setEducationInstitution(registrationDTO.getEducationInstitution());
        registration.setGraduationYear(registrationDTO.getGraduationYear());
        registration.setProfessionalCertifications(registrationDTO.getProfessionalCertifications());
        registration.setSkillsCompetencies(registrationDTO.getSkillsCompetencies());
        registration.setYearsOfExperience(registrationDTO.getYearsOfExperience());
        registration.setPreviousEmployer1(registrationDTO.getPreviousEmployer1());
        registration.setPreviousPosition1(registrationDTO.getPreviousPosition1());
        registration.setPreviousEmploymentDuration1(registrationDTO.getPreviousEmploymentDuration1());
        registration.setPreviousEmployer2(registrationDTO.getPreviousEmployer2());
        registration.setPreviousPosition2(registrationDTO.getPreviousPosition2());
        registration.setPreviousEmploymentDuration2(registrationDTO.getPreviousEmploymentDuration2());
        registration.setPreviousEmployer3(registrationDTO.getPreviousEmployer3());
        registration.setPreviousPosition3(registrationDTO.getPreviousPosition3());
        registration.setPreviousEmploymentDuration3(registrationDTO.getPreviousEmploymentDuration3());
        registration.setReference1Name(registrationDTO.getReference1Name());
        registration.setReference1Position(registrationDTO.getReference1Position());
        registration.setReference1Company(registrationDTO.getReference1Company());
        registration.setReference1Phone(registrationDTO.getReference1Phone());
        registration.setReference1Email(registrationDTO.getReference1Email());
        registration.setReference2Name(registrationDTO.getReference2Name());
        registration.setReference2Position(registrationDTO.getReference2Position());
        registration.setReference2Company(registrationDTO.getReference2Company());
        registration.setReference2Phone(registrationDTO.getReference2Phone());
        registration.setReference2Email(registrationDTO.getReference2Email());
        registration.setBloodType(registrationDTO.getBloodType());
        registration.setMedicalConditions(registrationDTO.getMedicalConditions());
        registration.setAllergies(registrationDTO.getAllergies());
        registration.setMedications(registrationDTO.getMedications());

        // Handle file uploads
        if (resumeCv != null && !resumeCv.isEmpty()) {
            String resumePath = fileStorageService.storeFile(resumeCv);
            registration.setResumeCv(resumePath);
        }
        if (nationalIdCopy != null && !nationalIdCopy.isEmpty()) {
            String nationalIdPath = fileStorageService.storeFile(nationalIdCopy);
            registration.setNationalIdCopy(nationalIdPath);
        }
        if (passportCopy != null && !passportCopy.isEmpty()) {
            registration.setPassportCopy(fileStorageService.storeFile(passportCopy));
        }
        if (driversLicenseCopy != null && !driversLicenseCopy.isEmpty()) {
            registration.setDriversLicenseCopy(fileStorageService.storeFile(driversLicenseCopy));
        }
        if (educationCertificates != null && !educationCertificates.isEmpty()) {
            registration.setEducationCertificates(fileStorageService.storeFile(educationCertificates));
        }
        if (professionalCertificatesFiles != null && !professionalCertificatesFiles.isEmpty()) {
            registration.setProfessionalCertificatesFiles(fileStorageService.storeFile(professionalCertificatesFiles));
        }
        if (medicalCertificate != null && !medicalCertificate.isEmpty()) {
            registration.setMedicalCertificate(fileStorageService.storeFile(medicalCertificate));
        }
        if (referenceLetters != null && !referenceLetters.isEmpty()) {
            registration.setReferenceLetters(fileStorageService.storeFile(referenceLetters));
        }
        if (otherDocuments != null && !otherDocuments.isEmpty()) {
            registration.setOtherDocuments(fileStorageService.storeFile(otherDocuments));
        }

        // Set default statuses
        registration.setRegistrationStatus(PersonRegistration.RegistrationStatus.PENDING_REVIEW);
        registration.setBackgroundCheckStatus(PersonRegistration.VerificationStatus.PENDING);
        registration.setMedicalCheckStatus(PersonRegistration.VerificationStatus.PENDING);
        registration.setDrugTestStatus(PersonRegistration.VerificationStatus.PENDING);
        registration.setReferenceCheckStatus(PersonRegistration.VerificationStatus.PENDING);

        // Set timestamps
        registration.setCreatedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());

        // Save the registration
        PersonRegistration savedRegistration = personRegistrationRepository.save(registration);

        // Send confirmation email
        sendRegistrationConfirmationEmail(savedRegistration);

        return mapToResponseDTO(savedRegistration);
    }

    private void sendRegistrationConfirmationEmail(PersonRegistration registration) {
        try {
            String subject = "Application Received";
            String message = String.format(
                    "Dear %s %s,\n\n" +
                            "Thank you for submitting your application for the position of %s.\n\n" +
                            "Your application has been received and is currently under review. " +
                            "We will contact you within 3-5 business days with updates regarding your application status.\n\n" +
                            "Application Details:\n" +
                            "Application ID: %d\n" +
                            "Position: %s\n" +
                            "Submission Date: %s\n\n" +
                            "If you have any questions, please contact our HR department at hr@security.com.\n\n" +
                            "Regards,\nSecurity Recruitment Team",
                    registration.getFirstName(),
                    registration.getLastName(),
                    registration.getPositionAppliedFor(),
                    registration.getId(),
                    registration.getPositionAppliedFor(),
                    registration.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            );

            emailService.sendSimpleEmail(registration.getEmail(), subject, message);
        } catch (Exception e) {
            log.error("Failed to send registration confirmation email to {}", registration.getEmail(), e);
        }
    }




    @Transactional
    public PersonRegistrationResponseDTO updateRegistration(Long id, PersonRegistrationUpdateDTO updateDTO) {
        PersonRegistration registration = personRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + id));

        // Update all fields from the DTO
        registration.setFirstName(updateDTO.getFirstName());
        registration.setMiddleName(updateDTO.getMiddleName());
        registration.setLastName(updateDTO.getLastName());
        registration.setEmail(updateDTO.getEmail());
        registration.setPhoneNumber(updateDTO.getPhoneNumber());
        registration.setAlternatePhone(updateDTO.getAlternatePhone());
        registration.setDateOfBirth(updateDTO.getDateOfBirth());
        registration.setGender(updateDTO.getGender());
        registration.setNationality(updateDTO.getNationality());
        registration.setMaritalStatus(updateDTO.getMaritalStatus());
        registration.setStreetAddress(updateDTO.getStreetAddress());
        registration.setCity(updateDTO.getCity());
        registration.setStateProvince(updateDTO.getStateProvince());
        registration.setPostalCode(updateDTO.getPostalCode());
        registration.setCountry(updateDTO.getCountry());
        registration.setNationalId(updateDTO.getNationalId());
        registration.setPassportNumber(updateDTO.getPassportNumber());
        registration.setPassportExpiryDate(updateDTO.getPassportExpiryDate());
        registration.setDriversLicense(updateDTO.getDriversLicense());
        registration.setDriversLicenseExpiry(updateDTO.getDriversLicenseExpiry());
        registration.setHeightCm(updateDTO.getHeightCm());
        registration.setWeightKg(updateDTO.getWeightKg());
        registration.setPrimaryLanguage(updateDTO.getPrimaryLanguage());
        registration.setOtherLanguages(updateDTO.getOtherLanguages());
        registration.setEmergencyContactName(updateDTO.getEmergencyContactName());
        registration.setEmergencyContactRelationship(updateDTO.getEmergencyContactRelationship());
        registration.setEmergencyPhone(updateDTO.getEmergencyPhone());
        registration.setEmergencyContactAddress(updateDTO.getEmergencyContactAddress());
        registration.setNextOfKinName(updateDTO.getNextOfKinName());
        registration.setNextOfKinRelationship(updateDTO.getNextOfKinRelationship());
        registration.setNextOfKinPhone(updateDTO.getNextOfKinPhone());
        registration.setNextOfKinAddress(updateDTO.getNextOfKinAddress());
        registration.setPositionAppliedFor(updateDTO.getPositionAppliedFor());
        registration.setDepartmentPreference(updateDTO.getDepartmentPreference());
        registration.setLocationPreference(updateDTO.getLocationPreference());
        registration.setAvailabilityStartDate(updateDTO.getAvailabilityStartDate());
        registration.setExpectedSalary(updateDTO.getExpectedSalary());
        registration.setWorkAuthorizationStatus(updateDTO.getWorkAuthorizationStatus());
        registration.setWorkPermitNumber(updateDTO.getWorkPermitNumber());
        registration.setWorkPermitExpiry(updateDTO.getWorkPermitExpiry());
        registration.setHighestEducationLevel(updateDTO.getHighestEducationLevel());
        registration.setEducationInstitution(updateDTO.getEducationInstitution());
        registration.setGraduationYear(updateDTO.getGraduationYear());
        registration.setProfessionalCertifications(updateDTO.getProfessionalCertifications());
        registration.setSkillsCompetencies(updateDTO.getSkillsCompetencies());
        registration.setYearsOfExperience(updateDTO.getYearsOfExperience());
        registration.setPreviousEmployer1(updateDTO.getPreviousEmployer1());
        registration.setPreviousPosition1(updateDTO.getPreviousPosition1());
        registration.setPreviousEmploymentDuration1(updateDTO.getPreviousEmploymentDuration1());
        registration.setPreviousEmployer2(updateDTO.getPreviousEmployer2());
        registration.setPreviousPosition2(updateDTO.getPreviousPosition2());
        registration.setPreviousEmploymentDuration2(updateDTO.getPreviousEmploymentDuration2());
        registration.setPreviousEmployer3(updateDTO.getPreviousEmployer3());
        registration.setPreviousPosition3(updateDTO.getPreviousPosition3());
        registration.setPreviousEmploymentDuration3(updateDTO.getPreviousEmploymentDuration3());
        registration.setReference1Name(updateDTO.getReference1Name());
        registration.setReference1Position(updateDTO.getReference1Position());
        registration.setReference1Company(updateDTO.getReference1Company());
        registration.setReference1Phone(updateDTO.getReference1Phone());
        registration.setReference1Email(updateDTO.getReference1Email());
        registration.setReference2Name(updateDTO.getReference2Name());
        registration.setReference2Position(updateDTO.getReference2Position());
        registration.setReference2Company(updateDTO.getReference2Company());
        registration.setReference2Phone(updateDTO.getReference2Phone());
        registration.setReference2Email(updateDTO.getReference2Email());
        registration.setBackgroundCheckStatus(updateDTO.getBackgroundCheckStatus());
        registration.setBackgroundCheckDate(updateDTO.getBackgroundCheckDate());
        registration.setBackgroundCheckNotes(updateDTO.getBackgroundCheckNotes());
        registration.setMedicalCheckStatus(updateDTO.getMedicalCheckStatus());
        registration.setMedicalCheckDate(updateDTO.getMedicalCheckDate());
        registration.setMedicalFitnessValidUntil(updateDTO.getMedicalFitnessValidUntil());
        registration.setDrugTestStatus(updateDTO.getDrugTestStatus());
        registration.setDrugTestDate(updateDTO.getDrugTestDate());
        registration.setReferenceCheckStatus(updateDTO.getReferenceCheckStatus());
        registration.setReferenceCheckDate(updateDTO.getReferenceCheckDate());
        registration.setReferenceCheckNotes(updateDTO.getReferenceCheckNotes());
        registration.setSecurityClearanceLevel(updateDTO.getSecurityClearanceLevel());
        registration.setSecurityClearanceExpiry(updateDTO.getSecurityClearanceExpiry());
        registration.setBloodType(updateDTO.getBloodType());
        registration.setMedicalConditions(updateDTO.getMedicalConditions());
        registration.setAllergies(updateDTO.getAllergies());
        registration.setMedications(updateDTO.getMedications());
        registration.setRegistrationStatus(updateDTO.getRegistrationStatus());
        registration.setRegistrationNotes(updateDTO.getRegistrationNotes());

        registration.setUpdatedAt(LocalDateTime.now());

        PersonRegistration updatedRegistration = personRegistrationRepository.save(registration);
        return mapToResponseDTO(updatedRegistration);
    }





    @Autowired
    public PersonRegistrationService(PersonRegistrationRepository personRegistrationRepository,
                                     FileStorageService fileStorageService,
                                     EmailService emailService) {
        this.personRegistrationRepository = personRegistrationRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public PersonRegistrationResponseDTO findById(Long id) {
        PersonRegistration registration = personRegistrationRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + id));
        return mapToResponseDTO(registration);
    }

    @Transactional(readOnly = true)
    public Page<PersonRegistrationSummaryDTO> findAll(Pageable pageable) {
        return personRegistrationRepository.findAll(pageable)
                .map(this::mapToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public Page<PersonRegistrationSummaryDTO> findByRegistrationStatus(PersonRegistration.RegistrationStatus status, Pageable pageable) {
        return personRegistrationRepository.findByRegistrationStatus(status, pageable)
                .map(this::mapToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public List<PersonRegistrationSummaryDTO> findByRegistrationStatusWithTrainingDetails(PersonRegistration.RegistrationStatus status) {
        return personRegistrationRepository.findByRegistrationStatusWithTrainingDetails(status)
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<PersonRegistrationSummaryDTO> search(String searchTerm, Pageable pageable) {
        return personRegistrationRepository.searchPersonRegistrations(searchTerm, pageable)
                .map(this::mapToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public Page<PersonRegistrationSummaryDTO> searchByStatusAndKeyword(PersonRegistration.RegistrationStatus status, String searchTerm, Pageable pageable) {
        return personRegistrationRepository.searchByStatusAndKeyword(status, searchTerm, pageable)
                .map(this::mapToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public List<PersonRegistrationSummaryDTO> findByBackgroundCheckStatus(PersonRegistration.VerificationStatus status) {
        return personRegistrationRepository.findByBackgroundCheckStatus(status)
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PersonRegistrationSummaryDTO> findByMedicalCheckStatus(PersonRegistration.VerificationStatus status) {
        return personRegistrationRepository.findByMedicalCheckStatus(status)
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PersonRegistrationSummaryDTO> findPersonsWithExpiringDocuments(LocalDate startDate, LocalDate endDate) {
        return personRegistrationRepository.findPersonsWithExpiringDocuments(startDate, endDate)
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countByRegistrationStatus(PersonRegistration.RegistrationStatus status) {
        return personRegistrationRepository.countByRegistrationStatus(status);
    }

    @Transactional(readOnly = true)
    public Map<PersonRegistration.RegistrationStatus, Long> getRegistrationStatusCounts() {
        return personRegistrationRepository.getRegistrationStatusCounts()
                .stream()
                .collect(Collectors.toMap(
                        arr -> (PersonRegistration.RegistrationStatus) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    @Transactional(readOnly = true)
    public Map<PersonRegistration.VerificationStatus, Long> getBackgroundCheckStatusCounts() {
        return personRegistrationRepository.getBackgroundCheckStatusCounts()
                .stream()
                .collect(Collectors.toMap(
                        arr -> (PersonRegistration.VerificationStatus) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    @Transactional(readOnly = true)
    public Page<PersonRegistrationSummaryDTO> findWithFilters(
            PersonRegistration.RegistrationStatus status,
            String nationality,
            String positionAppliedFor,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        return personRegistrationRepository.findWithFilters(
                        status,
                        nationality,
                        positionAppliedFor,
                        fromDate,
                        toDate,
                        pageable)
                .map(this::mapToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public Page<PersonRegistrationSummaryDTO> findByRegisteredBy(Long userId, Pageable pageable) {
        return personRegistrationRepository.findByRegisteredBy_Id(userId, pageable)
                .map(this::mapToSummaryDTO);
    }

    @Transactional(readOnly = true)
    public List<PersonRegistrationSummaryDTO> findRecentRegistrations(LocalDateTime since) {
        return personRegistrationRepository.findRecentRegistrations(since)
                .stream()
                .map(this::mapToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateRegistrationStatus(Long id, PersonRegistration.RegistrationStatus status, String notes) {
        PersonRegistration registration = personRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + id));

        PersonRegistration.RegistrationStatus previousStatus = registration.getRegistrationStatus();
        registration.setRegistrationStatus(status);
        registration.setRegistrationNotes(notes);
        personRegistrationRepository.save(registration);

        if (shouldSendStatusEmail(previousStatus, status)) {
            sendStatusNotificationEmail(registration);
        }
    }

    @Transactional
    public void updateVerificationStatus(
            Long id,
            PersonRegistration.VerificationStatus backgroundCheckStatus,
            PersonRegistration.VerificationStatus medicalCheckStatus,
            PersonRegistration.VerificationStatus drugTestStatus,
            PersonRegistration.VerificationStatus referenceCheckStatus) {

        PersonRegistration registration = personRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + id));

        boolean statusChanged = false;

        if (backgroundCheckStatus != null) {
            registration.setBackgroundCheckStatus(backgroundCheckStatus);
            registration.setBackgroundCheckDate(LocalDate.now());
            statusChanged = true;
        }

        if (medicalCheckStatus != null) {
            registration.setMedicalCheckStatus(medicalCheckStatus);
            registration.setMedicalCheckDate(LocalDate.now());
            statusChanged = true;
        }

        if (drugTestStatus != null) {
            registration.setDrugTestStatus(drugTestStatus);
            registration.setDrugTestDate(LocalDate.now());
            statusChanged = true;
        }

        if (referenceCheckStatus != null) {
            registration.setReferenceCheckStatus(referenceCheckStatus);
            registration.setReferenceCheckDate(LocalDate.now());
            statusChanged = true;
        }

        if (statusChanged) {
            personRegistrationRepository.save(registration);
            sendVerificationUpdateEmail(registration, backgroundCheckStatus, medicalCheckStatus,
                    drugTestStatus, referenceCheckStatus);
        }
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return personRegistrationRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByNationalId(String nationalId) {
        return personRegistrationRepository.existsByNationalId(nationalId);
    }

    @Transactional
    public void updateDocument(Long id, String documentType, MultipartFile file) throws IOException {
        PersonRegistration registration = personRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found"));

        String currentFilename = getDocumentFilename(registration, documentType);
        if (currentFilename != null) {
            fileStorageService.deleteFile(currentFilename);
        }

        String newFilename = fileStorageService.storeFile(file);
        setDocumentFilename(registration, documentType, newFilename);

        personRegistrationRepository.save(registration);
    }

    @Transactional
    public void deleteDocument(Long id, String documentType) throws IOException {
        PersonRegistration registration = personRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found"));

        String filename = getDocumentFilename(registration, documentType);
        if (filename != null) {
            fileStorageService.deleteFile(filename);
            setDocumentFilename(registration, documentType, null);
            personRegistrationRepository.save(registration);
        }
    }

    // Mapping methods
    private PersonRegistrationResponseDTO mapToResponseDTO(PersonRegistration registration) {
        if (registration == null) {
            return null;
        }

        return PersonRegistrationResponseDTO.builder()
                .id(registration.getId())
                .firstName(registration.getFirstName())
                .middleName(registration.getMiddleName())
                .lastName(registration.getLastName())
                .fullName(getFullName(registration))
                .email(registration.getEmail())
                .phoneNumber(registration.getPhoneNumber())
                .alternatePhone(registration.getAlternatePhone())
                .dateOfBirth(registration.getDateOfBirth())
                .age(calculateAge(registration))
                .gender(registration.getGender())
                .nationality(registration.getNationality())
                .maritalStatus(registration.getMaritalStatus())
                .streetAddress(registration.getStreetAddress())
                .city(registration.getCity())
                .stateProvince(registration.getStateProvince())
                .postalCode(registration.getPostalCode())
                .country(registration.getCountry())
                .fullAddress(getFullAddress(registration))
                .nationalId(registration.getNationalId())
                .passportNumber(registration.getPassportNumber())
                .passportExpiryDate(registration.getPassportExpiryDate())
                .driversLicense(registration.getDriversLicense())
                .driversLicenseExpiry(registration.getDriversLicenseExpiry())
                .heightCm(registration.getHeightCm())
                .weightKg(registration.getWeightKg())
                .primaryLanguage(registration.getPrimaryLanguage())
                .otherLanguages(registration.getOtherLanguages())
                .emergencyContactName(registration.getEmergencyContactName())
                .emergencyContactRelationship(registration.getEmergencyContactRelationship())
                .emergencyPhone(registration.getEmergencyPhone())
                .emergencyContactAddress(registration.getEmergencyContactAddress())
                .nextOfKinName(registration.getNextOfKinName())
                .nextOfKinRelationship(registration.getNextOfKinRelationship())
                .nextOfKinPhone(registration.getNextOfKinPhone())
                .nextOfKinAddress(registration.getNextOfKinAddress())
                .positionAppliedFor(registration.getPositionAppliedFor())
                .departmentPreference(registration.getDepartmentPreference())
                .locationPreference(registration.getLocationPreference())
                .availabilityStartDate(registration.getAvailabilityStartDate())
                .expectedSalary(registration.getExpectedSalary())
                .workAuthorizationStatus(registration.getWorkAuthorizationStatus())
                .workPermitNumber(registration.getWorkPermitNumber())
                .workPermitExpiry(registration.getWorkPermitExpiry())
                .highestEducationLevel(registration.getHighestEducationLevel())
                .educationInstitution(registration.getEducationInstitution())
                .graduationYear(registration.getGraduationYear())
                .professionalCertifications(registration.getProfessionalCertifications())
                .skillsCompetencies(registration.getSkillsCompetencies())
                .yearsOfExperience(registration.getYearsOfExperience())
                .previousEmployer1(registration.getPreviousEmployer1())
                .previousPosition1(registration.getPreviousPosition1())
                .previousEmploymentDuration1(registration.getPreviousEmploymentDuration1())
                .previousEmployer2(registration.getPreviousEmployer2())
                .previousPosition2(registration.getPreviousPosition2())
                .previousEmploymentDuration2(registration.getPreviousEmploymentDuration2())
                .previousEmployer3(registration.getPreviousEmployer3())
                .previousPosition3(registration.getPreviousPosition3())
                .previousEmploymentDuration3(registration.getPreviousEmploymentDuration3())
                .reference1Name(registration.getReference1Name())
                .reference1Position(registration.getReference1Position())
                .reference1Company(registration.getReference1Company())
                .reference1Phone(registration.getReference1Phone())
                .reference1Email(registration.getReference1Email())
                .reference2Name(registration.getReference2Name())
                .reference2Position(registration.getReference2Position())
                .reference2Company(registration.getReference2Company())
                .reference2Phone(registration.getReference2Phone())
                .reference2Email(registration.getReference2Email())
                .backgroundCheckStatus(registration.getBackgroundCheckStatus())
                .backgroundCheckDate(registration.getBackgroundCheckDate())
                .backgroundCheckNotes(registration.getBackgroundCheckNotes())
                .medicalCheckStatus(registration.getMedicalCheckStatus())
                .medicalCheckDate(registration.getMedicalCheckDate())
                .medicalFitnessValidUntil(registration.getMedicalFitnessValidUntil())
                .drugTestStatus(registration.getDrugTestStatus())
                .drugTestDate(registration.getDrugTestDate())
                .referenceCheckStatus(registration.getReferenceCheckStatus())
                .referenceCheckDate(registration.getReferenceCheckDate())
                .referenceCheckNotes(registration.getReferenceCheckNotes())
                .securityClearanceLevel(registration.getSecurityClearanceLevel())
                .securityClearanceExpiry(registration.getSecurityClearanceExpiry())
                .bloodType(registration.getBloodType())
                .medicalConditions(registration.getMedicalConditions())
                .allergies(registration.getAllergies())
                .medications(registration.getMedications())
                .profilePicture(registration.getProfilePicture())
                .resumeCv(registration.getResumeCv())
                .nationalIdCopy(registration.getNationalIdCopy())
                .passportCopy(registration.getPassportCopy())
                .driversLicenseCopy(registration.getDriversLicenseCopy())
                .educationCertificates(registration.getEducationCertificates())
                .professionalCertificatesFiles(registration.getProfessionalCertificatesFiles())
                .medicalCertificate(registration.getMedicalCertificate())
                .referenceLetters(registration.getReferenceLetters())
                .otherDocuments(registration.getOtherDocuments())
                .registrationStatus(registration.getRegistrationStatus())
                .registrationNotes(registration.getRegistrationNotes())
                .registeredByName(registration.getRegisteredBy() != null ?
                        registration.getRegisteredBy().getFirstName() : null)
                .createdAt(registration.getCreatedAt())
                .updatedAt(registration.getUpdatedAt())

                .build();
    }

    private PersonRegistrationSummaryDTO mapToSummaryDTO(PersonRegistration registration) {
        if (registration == null) {
            return null;
        }

        return PersonRegistrationSummaryDTO.builder()
                .id(registration.getId())
                .firstName(registration.getFirstName())
                .lastName(registration.getLastName())
                .fullName(getFullName(registration))
                .email(registration.getEmail())
                .phoneNumber(registration.getPhoneNumber())
                .positionAppliedFor(registration.getPositionAppliedFor())
                .registrationStatus(registration.getRegistrationStatus())
                .dateOfBirth(registration.getDateOfBirth())
                .age(calculateAge(registration))
                .nationality(registration.getNationality())
                .workAuthorizationStatus(registration.getWorkAuthorizationStatus())
                .backgroundCheckStatus(registration.getBackgroundCheckStatus())
                .medicalCheckStatus(registration.getMedicalCheckStatus())
                .drugTestStatus(registration.getDrugTestStatus())
                .referenceCheckStatus(registration.getReferenceCheckStatus())
                .hasTrainingRecord(registration.getTrainingRecord() != null)
                .isActiveGuard(registration.getGuard() != null && registration.getGuard().isArmed())
                .trainingStatus(getTrainingStatus(registration))
                .registeredByName(registration.getRegisteredBy() != null ?
                        registration.getRegisteredBy().getFirstName() : null)
                .createdAt(registration.getCreatedAt())
                .updatedAt(registration.getUpdatedAt())
                .build();
    }

    // Helper methods
    private String getFullName(PersonRegistration registration) {
        if (registration == null) {
            return null;
        }
        return String.format("%s %s", registration.getFirstName(), registration.getLastName());
    }

    private String getFullAddress(PersonRegistration registration) {
        if (registration == null) {
            return null;
        }
        return String.format("%s, %s, %s, %s",
                registration.getStreetAddress(),
                registration.getCity(),
                registration.getStateProvince(),
                registration.getCountry());
    }

    private Integer calculateAge(PersonRegistration registration) {
        if (registration == null || registration.getDateOfBirth() == null) {
            return null;
        }
        return Period.between(registration.getDateOfBirth(), LocalDate.now()).getYears();
    }

    private String getTrainingStatus(PersonRegistration registration) {
        if (registration == null || registration.getTrainingRecord() == null) {
            return "NOT_STARTED";
        }
        // Add your logic to determine training status
        return "COMPLETED"; // Example
    }

    private String getDocumentFilename(PersonRegistration registration, String documentType) {
        if (registration == null || documentType == null) {
            return null;
        }
        return switch (documentType.toLowerCase()) {
            case "profilepicture" -> registration.getProfilePicture();
            case "resumecv" -> registration.getResumeCv();
            case "nationalidcopy" -> registration.getNationalIdCopy();
            case "passportcopy" -> registration.getPassportCopy();
            case "driverslicensecopy" -> registration.getDriversLicenseCopy();
            case "educationcertificates" -> registration.getEducationCertificates();
            case "professionalcertificates" -> registration.getProfessionalCertificatesFiles();
            case "medicalcertificate" -> registration.getMedicalCertificate();
            case "referenceletters" -> registration.getReferenceLetters();
            case "otherdocuments" -> registration.getOtherDocuments();
            default -> null;
        };
    }

    private void setDocumentFilename(PersonRegistration registration, String documentType, String filename) {
        if (registration == null || documentType == null) {
            return;
        }
        switch (documentType.toLowerCase()) {
            case "profilepicture" -> registration.setProfilePicture(filename);
            case "resumecv" -> registration.setResumeCv(filename);
            case "nationalidcopy" -> registration.setNationalIdCopy(filename);
            case "passportcopy" -> registration.setPassportCopy(filename);
            case "driverslicensecopy" -> registration.setDriversLicenseCopy(filename);
            case "educationcertificates" -> registration.setEducationCertificates(filename);
            case "professionalcertificates" -> registration.setProfessionalCertificatesFiles(filename);
            case "medicalcertificate" -> registration.setMedicalCertificate(filename);
            case "referenceletters" -> registration.setReferenceLetters(filename);
            case "otherdocuments" -> registration.setOtherDocuments(filename);
        };
    }

    private boolean shouldSendStatusEmail(PersonRegistration.RegistrationStatus oldStatus, PersonRegistration.RegistrationStatus newStatus) {
        return newStatus != oldStatus &&
                (newStatus == PersonRegistration.RegistrationStatus.APPROVED_FOR_TRAINING ||
                        newStatus == PersonRegistration.RegistrationStatus.IN_TRAINING ||
                        newStatus == PersonRegistration.RegistrationStatus.TRAINING_COMPLETED ||
                        newStatus == PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD ||
                        newStatus == PersonRegistration.RegistrationStatus.REJECTED);
    }

    private void sendStatusNotificationEmail(PersonRegistration registration) {
        String subject = "Application Status Update";
        String message = buildStatusEmailMessage(registration);
        emailService.sendSimpleEmail(registration.getEmail(), subject, message);
    }

    private String buildStatusEmailMessage(PersonRegistration registration) {
        return switch (registration.getRegistrationStatus()) {
            case APPROVED_FOR_TRAINING -> buildTrainingApprovalEmail(registration);
            case IN_TRAINING -> buildTrainingStartEmail(registration);
            case TRAINING_COMPLETED -> buildTrainingCompletionEmail(registration);
            case APPROVED_FOR_GUARD -> buildGuardApprovalEmail(registration);
            case REJECTED -> buildRejectionEmail(registration);
            default -> String.format(
                    "Dear %s %s,\n\nYour application status has been updated to: %s\n\n" +
                            "Regards,\nSecurity Team",
                    registration.getFirstName(),
                    registration.getLastName(),
                    registration.getRegistrationStatus()
            );
        };
    }

    private String buildTrainingApprovalEmail(PersonRegistration registration) {
        return String.format(
                "Dear %s %s,\n\n" +
                        "Congratulations! Your application has been approved for training.\n\n" +
                        "Next Steps:\n" +
                        "1. You will receive training schedule details within 3 business days\n" +
                        "2. Please bring your original ID documents\n" +
                        "3. Dress code: Business casual\n\n" +
                        "If you have any questions, please contact HR at hr@security.com.\n\n" +
                        "Regards,\nSecurity Training Team",
                registration.getFirstName(),
                registration.getLastName()
        );
    }

    private String buildTrainingStartEmail(PersonRegistration registration) {
        return String.format(
                "Dear %s %s,\n\n" +
                        "Your security training program is scheduled to begin.\n\n" +
                        "Important Information:\n" +
                        "- Start Date: [Training Start Date]\n" +
                        "- Location: [Training Center Address]\n" +
                        "- Duration: [Number] weeks\n" +
                        "- Required Items: [List of required items]\n\n" +
                        "Please confirm your attendance by replying to this email.\n\n" +
                        "Regards,\nSecurity Training Team",
                registration.getFirstName(),
                registration.getLastName()
        );
    }

    private String buildTrainingCompletionEmail(PersonRegistration registration) {
        return String.format(
                "Dear %s %s,\n\n" +
                        "Congratulations on successfully completing your security training!\n\n" +
                        "Next Steps:\n" +
                        "1. Your certification will be processed within 5 business days\n" +
                        "2. You will be contacted regarding guard assignment\n" +
                        "3. Please ensure all your documents are up to date\n\n" +
                        "Regards,\nSecurity Training Team",
                registration.getFirstName(),
                registration.getLastName()
        );
    }

    private String buildGuardApprovalEmail(PersonRegistration registration) {
        return String.format(
                "Dear %s %s,\n\n" +
                        "We are pleased to inform you that you have been approved as a security guard!\n\n" +
                        "Next Steps:\n" +
                        "1. You will receive your guard ID and assignment details shortly\n" +
                        "2. Please complete the onboarding process at our office\n" +
                        "3. Uniform fitting will be scheduled\n\n" +
                        "Welcome to the team!\n\n" +
                        "Regards,\nSecurity Operations Team",
                registration.getFirstName(),
                registration.getLastName()
        );
    }

    private String buildRejectionEmail(PersonRegistration registration) {
        return String.format(
                "Dear %s %s,\n\n" +
                        "After careful consideration, we regret to inform you that your application " +
                        "has not been successful at this time.\n\n" +
                        "Reason: %s\n\n" +
                        "We appreciate your interest in our organization and encourage you to " +
                        "apply again in the future if your circumstances change.\n\n" +
                        "Thank you,\nSecurity Recruitment Team",
                registration.getFirstName(),
                registration.getLastName(),
                registration.getRegistrationNotes() != null ?
                        registration.getRegistrationNotes() : "Not specified"
        );
    }

    private void sendVerificationUpdateEmail(
            PersonRegistration registration,
            PersonRegistration.VerificationStatus backgroundCheckStatus,
            PersonRegistration.VerificationStatus medicalCheckStatus,
            PersonRegistration.VerificationStatus drugTestStatus,
            PersonRegistration.VerificationStatus referenceCheckStatus) {

        StringBuilder message = new StringBuilder();
        message.append(String.format("Dear %s %s,\n\n", registration.getFirstName(), registration.getLastName()));
        message.append("The status of your verification checks has been updated:\n\n");

        if (backgroundCheckStatus != null) {
            message.append(String.format("- Background Check: %s\n", backgroundCheckStatus));
        }
        if (medicalCheckStatus != null) {
            message.append(String.format("- Medical Check: %s\n", medicalCheckStatus));
        }
        if (drugTestStatus != null) {
            message.append(String.format("- Drug Test: %s\n", drugTestStatus));
        }
        if (referenceCheckStatus != null) {
            message.append(String.format("- Reference Check: %s\n", referenceCheckStatus));
        }

        message.append("\n");

        boolean allPassed = (backgroundCheckStatus == null || backgroundCheckStatus == PersonRegistration.VerificationStatus.COMPLETED_PASS) &&
                (medicalCheckStatus == null || medicalCheckStatus == PersonRegistration.VerificationStatus.COMPLETED_PASS) &&
                (drugTestStatus == null || drugTestStatus == PersonRegistration.VerificationStatus.COMPLETED_PASS) &&
                (referenceCheckStatus == null || referenceCheckStatus == PersonRegistration.VerificationStatus.COMPLETED_PASS);

        boolean anyFailed = (backgroundCheckStatus == PersonRegistration.VerificationStatus.COMPLETED_FAIL) ||
                (medicalCheckStatus == PersonRegistration.VerificationStatus.COMPLETED_FAIL) ||
                (drugTestStatus == PersonRegistration.VerificationStatus.COMPLETED_FAIL) ||
                (referenceCheckStatus == PersonRegistration.VerificationStatus.COMPLETED_FAIL);

        if (allPassed) {
            message.append("All your verifications have been completed successfully!\n");
        } else if (anyFailed) {
            message.append("Some verifications require attention. Please contact HR for more information.\n");
        }

        message.append("\nRegards,\nSecurity Compliance Team");

        emailService.sendSimpleEmail(
                registration.getEmail(),
                "Verification Status Update",
                message.toString()
        );
    }
}