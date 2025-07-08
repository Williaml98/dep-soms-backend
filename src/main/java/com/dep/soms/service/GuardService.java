package com.dep.soms.service;

import com.dep.soms.dto.auth.SignupRequest;
import com.dep.soms.dto.guard.GuardDto;
import com.dep.soms.dto.guard.GuardRegistrationRequest;
import com.dep.soms.dto.guard.GuardUpdateRequest;
import com.dep.soms.dto.person.PersonRegistrationDto;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Guard;
import com.dep.soms.model.PersonRegistration;
import com.dep.soms.model.Role;
import com.dep.soms.model.User;
import com.dep.soms.repository.*;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GuardService {
    private static final Logger logger = LoggerFactory.getLogger(GuardService.class);
    @Autowired
    private GuardRepository guardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private PersonRegistrationRepository personRegistrationRepository;

    private static final String BADGE_PREFIX = "DEP-";
    private static final int BADGE_NUMBER_LENGTH = 4;

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;
    @Transactional(readOnly = true)
    public List<GuardDto> getAllGuards() {
        return guardRepository.findAll().stream()
                .map(this::mapGuardToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public GuardDto getGuardById(Long id) {
        Guard guard = guardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + id));
        return mapGuardToDto(guard);
    }

        @Transactional
    public GuardDto registerGuard(GuardRegistrationRequest request) {
        // First create the user
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        if (guardRepository.findByBadgeNumber(request.getBadgeNumber()).isPresent()) {
            throw new IllegalArgumentException("Badge number is already in use");
        }

        LocalDateTime now = LocalDateTime.now();

        // Store the original password for notification
        String originalPassword = request.getPassword();

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(originalPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .createdAt(now)
                .build();

        // Assign GUARD role
        Set<Role> roles = new HashSet<>();
        Role guardRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
                .orElseThrow(() -> new RuntimeException("Error: Guard Role is not found."));
        roles.add(guardRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Now create the guard
        Guard guard = Guard.builder()
                .user(savedUser)
                .badgeNumber(request.getBadgeNumber())
                .licenseNumber(request.getLicenseNumber())
                .licenseExpiry(request.getLicenseExpiry())
                .emergencyContact(request.getEmergencyContact())
                .emergencyPhone(request.getEmergencyPhone())
                .status(request.getStatus())
                .createdAt(now)
                .build();

        Guard savedGuard = guardRepository.save(guard);

        // Send notifications
        emailService.sendAccountCreationEmail(
                request.getEmail(),
                request.getUsername(),
                originalPassword,
                "Guard"
        );

        smsService.sendAccountCreationSms(
                request.getPhoneNumber(),
                request.getUsername(),
                "Guard"
        );

        return mapGuardToDto(savedGuard);
    }

//    private String generateBadgeNumber() {
//        // Get the highest current badge number
//        Optional<Guard> lastGuard = guardRepository.findTopByOrderByIdDesc();
//        int nextNumber = 1; // Default starting number
//
//        if (lastGuard.isPresent()) {
//            // Extract number from existing badge (format: DEP-0001)
//            String lastBadge = lastGuard.get().getBadgeNumber();
//            if (lastBadge != null && lastBadge.startsWith("DEP-")) {
//                try {
//                    nextNumber = Integer.parseInt(lastBadge.substring(4)) + 1;
//                } catch (NumberFormatException e) {
//                    // If parsing fails, just increment from the last ID
//                    nextNumber = lastGuard.get().getId().intValue() + 1;
//                }
//            } else {
//                nextNumber = lastGuard.get().getId().intValue() + 1;
//            }
//        }
//
//        // Format with leading zeros (DEP-0001, DEP-0002, etc.)
//        return String.format("DEP-%04d", nextNumber);
//    }

    private String generateBadgeNumber() {
        try {
            // Get the highest numeric part of existing badge numbers
            Optional<Integer> maxNumber = guardRepository.findMaxBadgeNumber();
            int nextNumber = maxNumber.orElse(0) + 1;

            // Validate the number isn't too large
            if (nextNumber > Math.pow(10, BADGE_NUMBER_LENGTH) - 1) {
                throw new IllegalStateException("Maximum badge number reached");
            }

            String badgeNumber = BADGE_PREFIX + String.format("%0" + BADGE_NUMBER_LENGTH + "d", nextNumber);

            // Double-check the badge number doesn't exist (just in case)
            if (guardRepository.findByBadgeNumber(badgeNumber).isPresent()) {
                throw new IllegalStateException("Generated badge number already exists: " + badgeNumber);
            }

            return badgeNumber;
        } catch (Exception e) {
            logger.error("Failed to generate badge number", e);
            throw new ServiceException("Could not generate badge number", e);
        }
    }

    @Transactional
    public GuardDto updateGuard(Long id, GuardUpdateRequest request) {
        Guard guard = guardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + id));

        User user = guard.getUser();

        // Update user information
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);

        // Update guard information
        if (request.getBadgeNumber() != null && !request.getBadgeNumber().equals(guard.getBadgeNumber())) {
            Optional<Guard> existingGuard = guardRepository.findByBadgeNumber(request.getBadgeNumber());
            if (existingGuard.isPresent() && !existingGuard.get().getId().equals(id)) {
                throw new IllegalArgumentException("Badge number is already in use");
            }
            guard.setBadgeNumber(request.getBadgeNumber());
        }

        if (request.getLicenseNumber() != null) {
            guard.setLicenseNumber(request.getLicenseNumber());
        }

        if (request.getLicenseExpiry() != null) {
            guard.setLicenseExpiry(request.getLicenseExpiry());
        }

        if (request.getEmergencyContact() != null) {
            guard.setEmergencyContact(request.getEmergencyContact());
        }

        if (request.getEmergencyPhone() != null) {
            guard.setEmergencyPhone(request.getEmergencyPhone());
        }

        if (request.getStatus() != null) {
            guard.setStatus(request.getStatus());
        }

        Guard updatedGuard = guardRepository.save(guard);
        return mapGuardToDto(updatedGuard);
    }

    // Add to GuardService.java

    // In GuardService.java
    @Transactional(readOnly = true)
    public List<GuardDto> getGuardsByClientId(Long clientId) {
        List<Guard> guards = guardRepository.findGuardsByClientId(clientId);
        logger.info("Fetched {} guards for client ID {}", guards.size(), clientId); // 👈 Logging here
        return guards.stream()
                .map(this::mapGuardToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteGuard(Long id) {
        Guard guard = guardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + id));

        User user = guard.getUser();
        user.setActive(false);
        userRepository.save(user);

        guard.setStatus(Guard.GuardStatus.INACTIVE);
        guardRepository.save(guard);
    }
    @Transactional(readOnly = true)
    public List<GuardDto> getAvailableGuards(String dateStr, String shiftId) {
        // This is a placeholder implementation
        // In a real application, you would check shift assignments and availability
        return getAllGuards().stream()
                .filter(guard -> Guard.GuardStatus.ACTIVE.name().equals(guard.getStatus().name()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GuardDto getGuardByUserId(Long userId) {
        Guard guard = guardRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Guard not found for user ID: " + userId));
        return mapGuardToDto(guard);
    }

//    private GuardDto mapGuardToDto(Guard guard) {
//        User user = guard.getUser();
//
//        GuardDto.UserDto userDto = GuardDto.UserDto.builder()
//                .id(user.getId())
//                .firstName(user.getFirstName())
//                .lastName(user.getLastName())
//                .email(user.getEmail())
//                .phoneNumber(user.getPhoneNumber())
//                .profilePicture(user.getProfilePicture())
//                .build();
//
//        return GuardDto.builder()
//                .id(guard.getId())
//                .user(userDto)  // Include nested user object
//                .badgeNumber(guard.getBadgeNumber())
//                .licenseNumber(guard.getLicenseNumber())
//                .licenseExpiry(guard.getLicenseExpiry())
//                .emergencyContact(guard.getEmergencyContact())
//                .emergencyPhone(guard.getEmergencyPhone())
//                .status(guard.getStatus())
//                .hireDate(guard.getCreatedAt() != null ? guard.getCreatedAt().toLocalDate() : null)
//                .build();
//
//    }


    private GuardDto mapGuardToDto(Guard guard) {
        User user = guard.getUser();
        PersonRegistration person = guard.getPersonRegistration();

        GuardDto.UserDto userDto = user != null ? GuardDto.UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .build() : null;

        return GuardDto.builder()
                .id(guard.getId())
                .userId(user != null ? user.getId() : null)
                .personRegistrationId(person != null ? person.getId() : null)
                .user(userDto)
                .firstName(person != null ? person.getFirstName() : (user != null ? user.getFirstName() : null))
                .lastName(person != null ? person.getLastName() : (user != null ? user.getLastName() : null))
                .email(person != null ? person.getEmail() : (user != null ? user.getEmail() : null))
                .phoneNumber(person != null ? person.getPhoneNumber() : (user != null ? user.getPhoneNumber() : null))
                .badgeNumber(guard.getBadgeNumber())
                .licenseNumber(guard.getLicenseNumber())
                .licenseExpiry(guard.getLicenseExpiry())
                .emergencyContact(guard.getEmergencyContact())
                .emergencyPhone(guard.getEmergencyPhone())
                .status(guard.getStatus())
                .hireDate(guard.getCreatedAt() != null ? guard.getCreatedAt().toLocalDate() : null)
                .build();
    }

    @Transactional
    public GuardDto createGuard(GuardRegistrationRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Create user account
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .build();

        // Set role
        Set<Role> roles = new HashSet<>();
        Role guardRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
                .orElseThrow(() -> new RuntimeException("Error: Role GUARD is not found."));
        roles.add(guardRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Create guard profile
        Guard guard = Guard.builder()
                .user(savedUser)
                .build();

        Guard savedGuard = guardRepository.save(guard);
        return mapGuardToDto(savedGuard);
    }

    @Transactional(readOnly = true)
    public List<GuardDto> getAvailableGuards(String dateStr, String startTimeStr, String endTimeStr) {
        LocalDate date = LocalDate.parse(dateStr);
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);

        List<Guard> allGuards = guardRepository.findByUser_ActiveTrue();
        return allGuards.stream()
                .filter(guard -> isGuardAvailable(guard, date, startTime, endTime))
                .map(this::mapGuardToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    private boolean isGuardAvailable(Guard guard, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Get all assignments for this guard on the specified date
        var assignments = shiftAssignmentRepository.findByGuardAndAssignmentDate(guard, date);

        // Check if any assignment overlaps with the requested time slot
        for (var assignment : assignments) {
            LocalTime assignmentStart = assignment.getStartTime() != null ?
                    LocalTime.from(assignment.getStartTime()) : assignment.getShift().getStartTime();
            LocalTime assignmentEnd = assignment.getEndTime() != null ?
                    LocalTime.from(assignment.getEndTime()) : assignment.getShift().getEndTime();

            // Check for overlap
            boolean overlaps = (startTime.isBefore(assignmentEnd) || startTime.equals(assignmentEnd)) &&
                    (endTime.isAfter(assignmentStart) || endTime.equals(assignmentStart));

            if (overlaps) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get all person registrations that are approved for guard but don't have a guard record yet
     */
//    @Transactional(readOnly = true)
//    public List<PersonRegistration> getApprovedForGuardPersons() {
//        return personRegistrationRepository.findByRegistrationStatusAndGuardIsNull(
//                PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD
//        );
//    }

    @Transactional(readOnly = true)
    public List<PersonRegistrationDto> getApprovedForGuardPersons() {
        List<PersonRegistration> persons = personRegistrationRepository.findByRegistrationStatusAndGuardIsNull(
                PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD
        );

        return persons.stream()
                .map(this::mapToPersonRegistrationDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a guard from an approved person registration
     */
//    @Transactional
//    public GuardDto createGuardFromPersonRegistration(Long personRegistrationId) {
//        PersonRegistration person = personRegistrationRepository.findById(personRegistrationId)
//                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + personRegistrationId));
//
//        // Verify the person is approved for guard
//        if (person.getRegistrationStatus() != PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD) {
//            throw new IllegalStateException("Person is not approved for guard status");
//        }
//
//        // Check if this person already has a guard record
//        if (person.getGuard() != null) {
//            throw new IllegalStateException("This person already has a guard record");
//        }
//
//        // Generate a badge number (you might want to implement a better badge number generation)
//        String badgeNumber = "G-" + System.currentTimeMillis();
//
//        // Create the guard entity
//        Guard guard = Guard.builder()
//                .personRegistration(person)
//                .badgeNumber(badgeNumber)
//                .hireDate(LocalDate.now())
//                .emergencyContact(person.getEmergencyContactName())
//                .emergencyPhone(person.getEmergencyPhone())
//                .status(Guard.GuardStatus.ACTIVE)
//                .build();
//
//        // Save the guard
//        Guard savedGuard = guardRepository.save(guard);
//
//        // Update the person's status to ACTIVE_GUARD
//        person.setRegistrationStatus(PersonRegistration.RegistrationStatus.ACTIVE_GUARD);
//        personRegistrationRepository.save(person);
//
//        // If the person has a user account, link it to the guard
//        if (person.getUser() != null) {
//            savedGuard.setUser(person.getUser());
//            // Assign GUARD role if not already present
//            Set<Role> roles = person.getUser().getRoles();
//            Role guardRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
//                    .orElseThrow(() -> new RuntimeException("Error: Guard Role is not found."));
//            roles.add(guardRole);
//            person.getUser().setRoles(roles);
//            userRepository.save(person.getUser());
//            savedGuard = guardRepository.save(savedGuard);
//        }
//
//        return mapGuardToDto(savedGuard);
//    }

    @Transactional
    public GuardDto createGuardFromPersonRegistration(Long personRegistrationId) {
        PersonRegistration person = personRegistrationRepository.findById(personRegistrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + personRegistrationId));

        // Verify the person is approved for guard
        if (person.getRegistrationStatus() != PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD) {
            throw new IllegalStateException("Person is not approved for guard status");
        }

        // Check if this person already has a guard record
        if (person.getGuard() != null) {
            throw new IllegalStateException("This person already has a guard record");
        }

        // Generate badge number
        String badgeNumber = generateBadgeNumber();

        // Create the guard entity
        Guard guard = Guard.builder()
                .personRegistration(person)
                .badgeNumber(badgeNumber)
                .hireDate(LocalDate.now())
                .emergencyContact(person.getEmergencyContactName())
                .emergencyPhone(person.getEmergencyPhone())
                .status(Guard.GuardStatus.ACTIVE)
                .build();

        // Save the guard
        Guard savedGuard = guardRepository.save(guard);

        // Update the person's status to ACTIVE_GUARD
        person.setRegistrationStatus(PersonRegistration.RegistrationStatus.ACTIVE_GUARD);
        personRegistrationRepository.save(person);

        // Handle user account creation if not exists
        if (person.getUser() == null) {
            // Create a new user account
            String username = person.getEmail().split("@")[0]; // Use email prefix as username
            String tempPassword = generateRandomPassword(); // Implement this method

            User user = User.builder()
                    .username(username)
                    .email(person.getEmail())
                    .password(passwordEncoder.encode(tempPassword))
                    .firstName(person.getFirstName())
                    .lastName(person.getLastName())
                    .phoneNumber(person.getPhoneNumber())
                    .active(true)
                    .build();

            // Assign GUARD role
            Set<Role> roles = new HashSet<>();
            Role guardRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
                    .orElseThrow(() -> new RuntimeException("Error: Guard Role is not found."));
            roles.add(guardRole);
            user.setRoles(roles);

            User savedUser = userRepository.save(user);
            savedGuard.setUser(savedUser);
            savedGuard = guardRepository.save(savedGuard);

            // Link user to person registration
            person.setUser(savedUser);
            personRegistrationRepository.save(person);

            // Send account creation email
            emailService.sendAccountCreationEmail(
                    person.getEmail(),
                    username,
                    tempPassword,
                    "Guard",
                    badgeNumber
            );
        } else {
            // For existing user, just add GUARD role if not present
            User user = person.getUser();
            Set<Role> roles = user.getRoles();
            Role guardRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
                    .orElseThrow(() -> new RuntimeException("Error: Guard Role is not found."));

            if (!roles.contains(guardRole)) {
                roles.add(guardRole);
                user.setRoles(roles);
                userRepository.save(user);
            }

            savedGuard.setUser(user);
            savedGuard = guardRepository.save(savedGuard);

            // Send guard activation email
            emailService.sendGuardActivationEmail(
                    person.getEmail(),
                    person.getFirstName() + " " + person.getLastName(),
                    badgeNumber
            );
        }

        return mapGuardToDto(savedGuard);
    }

    private String generateRandomPassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String combined = upperCaseLetters + lowerCaseLetters + numbers;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));

        for (int i = 0; i < 5; i++) {
            password.append(combined.charAt(random.nextInt(combined.length())));
        }

        return password.toString();
    }

    @Transactional
    public void softDeleteGuard(Long id) {
        Guard guard = guardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found with id: " + id));

        // Set guard status to INACTIVE
        guard.setStatus(Guard.GuardStatus.INACTIVE);
        guardRepository.save(guard);

        // If there's an associated user, deactivate it
        if (guard.getUser() != null) {
            User user = guard.getUser();
            user.setActive(false);
            userRepository.save(user);
        }

        // If there's an associated person registration, update its status
        if (guard.getPersonRegistration() != null) {
            PersonRegistration person = guard.getPersonRegistration();
            person.setRegistrationStatus(PersonRegistration.RegistrationStatus.INACTIVE);
            personRegistrationRepository.save(person);
        }
    }





    private PersonRegistrationDto mapToPersonRegistrationDto(PersonRegistration person) {
        return PersonRegistrationDto.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .lastName(person.getLastName())
                .email(person.getEmail())
                .phoneNumber(person.getPhoneNumber())
                .alternatePhone(person.getAlternatePhone())
                .dateOfBirth(person.getDateOfBirth())
                .gender(person.getGender())
                .nationality(person.getNationality())
                .maritalStatus(person.getMaritalStatus())
                .streetAddress(person.getStreetAddress())
                .city(person.getCity())
                .stateProvince(person.getStateProvince())
                .postalCode(person.getPostalCode())
                .country(person.getCountry())
                .nationalId(person.getNationalId())
                .passportNumber(person.getPassportNumber())
                .passportExpiryDate(person.getPassportExpiryDate())
                .driversLicense(person.getDriversLicense())
                .driversLicenseExpiry(person.getDriversLicenseExpiry())
                .heightCm(person.getHeightCm())
                .weightKg(person.getWeightKg())
                .primaryLanguage(person.getPrimaryLanguage())
                .otherLanguages(person.getOtherLanguages())
                .emergencyContactName(person.getEmergencyContactName())
                .emergencyContactRelationship(person.getEmergencyContactRelationship())
                .emergencyPhone(person.getEmergencyPhone())
                .emergencyContactAddress(person.getEmergencyContactAddress())
                .nextOfKinName(person.getNextOfKinName())
                .nextOfKinRelationship(person.getNextOfKinRelationship())
                .nextOfKinPhone(person.getNextOfKinPhone())
                .nextOfKinAddress(person.getNextOfKinAddress())
                .positionAppliedFor(person.getPositionAppliedFor())
                .departmentPreference(person.getDepartmentPreference())
                .locationPreference(person.getLocationPreference())
                .availabilityStartDate(person.getAvailabilityStartDate())
                .expectedSalary(person.getExpectedSalary())
                .workAuthorizationStatus(person.getWorkAuthorizationStatus())
                .workPermitNumber(person.getWorkPermitNumber())
                .workPermitExpiry(person.getWorkPermitExpiry())
                .highestEducationLevel(person.getHighestEducationLevel())
                .educationInstitution(person.getEducationInstitution())
                .graduationYear(person.getGraduationYear())
                .professionalCertifications(person.getProfessionalCertifications())
                .skillsCompetencies(person.getSkillsCompetencies())
                .yearsOfExperience(person.getYearsOfExperience())
                .previousEmployer1(person.getPreviousEmployer1())
                .previousPosition1(person.getPreviousPosition1())
                .previousEmploymentDuration1(person.getPreviousEmploymentDuration1())
                .previousEmployer2(person.getPreviousEmployer2())
                .previousPosition2(person.getPreviousPosition2())
                .previousEmploymentDuration2(person.getPreviousEmploymentDuration2())
                .previousEmployer3(person.getPreviousEmployer3())
                .previousPosition3(person.getPreviousPosition3())
                .previousEmploymentDuration3(person.getPreviousEmploymentDuration3())
                .reference1Name(person.getReference1Name())
                .reference1Position(person.getReference1Position())
                .reference1Company(person.getReference1Company())
                .reference1Phone(person.getReference1Phone())
                .reference1Email(person.getReference1Email())
                .reference2Name(person.getReference2Name())
                .reference2Position(person.getReference2Position())
                .reference2Company(person.getReference2Company())
                .reference2Phone(person.getReference2Phone())
                .reference2Email(person.getReference2Email())
                .backgroundCheckStatus(person.getBackgroundCheckStatus())
                .backgroundCheckDate(person.getBackgroundCheckDate())
                .backgroundCheckNotes(person.getBackgroundCheckNotes())
                .medicalCheckStatus(person.getMedicalCheckStatus())
                .medicalCheckDate(person.getMedicalCheckDate())
                .medicalFitnessValidUntil(person.getMedicalFitnessValidUntil())
                .drugTestStatus(person.getDrugTestStatus())
                .drugTestDate(person.getDrugTestDate())
                .referenceCheckStatus(person.getReferenceCheckStatus())
                .referenceCheckDate(person.getReferenceCheckDate())
                .referenceCheckNotes(person.getReferenceCheckNotes())
                .securityClearanceLevel(person.getSecurityClearanceLevel())
                .securityClearanceExpiry(person.getSecurityClearanceExpiry())
                .backgroundCheckConsent(person.getBackgroundCheckConsent())
                .dataProcessingConsent(person.getDataProcessingConsent())
                .termsConditionsAccepted(person.getTermsConditionsAccepted())
                .privacyPolicyAccepted(person.getPrivacyPolicyAccepted())
                .consentDate(person.getConsentDate())
                .bloodType(person.getBloodType())
                .medicalConditions(person.getMedicalConditions())
                .allergies(person.getAllergies())
                .medications(person.getMedications())
                .profilePicture(person.getProfilePicture())
                .resumeCv(person.getResumeCv())
                .nationalIdCopy(person.getNationalIdCopy())
                .passportCopy(person.getPassportCopy())
                .driversLicenseCopy(person.getDriversLicenseCopy())
                .educationCertificates(person.getEducationCertificates())
                .professionalCertificatesFiles(person.getProfessionalCertificatesFiles())
                .medicalCertificate(person.getMedicalCertificate())
                .referenceLetters(person.getReferenceLetters())
                .otherDocuments(person.getOtherDocuments())
                .registrationNotes(person.getRegistrationNotes())
                .registrationStatus(person.getRegistrationStatus())
                .registeredById(person.getRegisteredBy() != null ? person.getRegisteredBy().getId() : null)
                .registeredByUsername(person.getRegisteredBy() != null ? person.getRegisteredBy().getUsername() : null)
                .userId(person.getUser() != null ? person.getUser().getId() : null)
                .guardId(person.getGuard() != null ? person.getGuard().getId() : null)
                .trainingRecordId(person.getTrainingRecord() != null ? person.getTrainingRecord().getId() : null)
                .createdAt(person.getCreatedAt())
                .updatedAt(person.getUpdatedAt())
                .build();
    }




}
