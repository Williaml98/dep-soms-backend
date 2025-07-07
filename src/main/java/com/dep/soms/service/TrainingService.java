//package com.dep.soms.service;
//
//import com.dep.soms.dto.person.*;
//import com.dep.soms.exception.ResourceNotFoundException;
//import com.dep.soms.model.*;
//import com.dep.soms.repository.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class TrainingService {
//    private final TrainingSessionRepository trainingSessionRepository;
//    private final TrainingAttendanceRepository trainingAttendanceRepository;
//    private final TrainingRecordRepository trainingRecordRepository;
//    private final PersonRegistrationRepository personRegistrationRepository;
//    private final CertificationRepository certificationRepository;
//    private final TrainingCertificationRepository trainingCertificationRepository;
//    private final UserRepository userRepository;
//
//    // Training Session methods
//    @Transactional
//    public List<TrainingSessionResponseDTO> getAllTrainingSessions() {
//        return trainingSessionRepository.findAll().stream()
//                .map(this::mapToTrainingSessionDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public TrainingSessionResponseDTO getTrainingSessionById(Long id) {
//        TrainingSession session = trainingSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
//        return mapToTrainingSessionDTO(session);
//    }
//
//    @Transactional
//    public List<TrainingAttendanceResponseDTO> assignRecruitsToTrainingSession(Long sessionId, List<Long> recruitIds) {
//        TrainingSession session = trainingSessionRepository.findById(sessionId)
//                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + sessionId));
//
//        List<TrainingAttendanceResponseDTO> results = new ArrayList<>();
//
//        for (Long recruitId : recruitIds) {
//            PersonRegistration recruit = personRegistrationRepository.findById(recruitId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Recruit not found with id: " + recruitId));
//
//            // Check if recruit is already assigned to this session
//            if (trainingAttendanceRepository.existsByTrainingSessionAndPersonRegistration(session, recruit)) {
//                continue;
//            }
//
//            // Create the attendance record
//            TrainingAttendanceCreateDTO dto = new TrainingAttendanceCreateDTO();
//            dto.setTrainingSessionId(sessionId);
//            dto.setPersonRegistrationId(recruitId);
//
//            TrainingAttendanceResponseDTO attendance = createTrainingAttendance(dto);
//            results.add(attendance);
//
//            // Update recruit status to IN_TRAINING if they're approved for training
//            if (recruit.getRegistrationStatus() == PersonRegistration.RegistrationStatus.APPROVED_FOR_TRAINING) {
//                recruit.setRegistrationStatus(PersonRegistration.RegistrationStatus.IN_TRAINING);
//                personRegistrationRepository.save(recruit);
//            }
//        }
//
//        return results;
//    }
//
//    @Transactional
//    public TrainingSessionResponseDTO createTrainingSession(TrainingSessionCreateDTO dto) {
//        User trainer = userRepository.findById(dto.getTrainerId())
//                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + dto.getTrainerId()));
//
//        TrainingSession session = TrainingSession.builder()
//                .title(dto.getTitle())
//                .description(dto.getDescription())
//                .trainer(trainer)
//                .startTime(dto.getStartTime())
//                .endTime(dto.getEndTime())
//                .location(dto.getLocation())
//                .maxParticipants(dto.getMaxParticipants())
//                .status(TrainingSession.TrainingStatus.SCHEDULED)
//                .build();
//
//        if (dto.getCertificationIds() != null && !dto.getCertificationIds().isEmpty()) {
//            dto.getCertificationIds().forEach(certId -> {
//                Certification cert = certificationRepository.findById(certId)
//                        .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certId));
//                session.getCertifications().add(cert);
//            });
//        }
//
//        TrainingSession savedSession = trainingSessionRepository.save(session);
//        return mapToTrainingSessionDTO(savedSession);
//    }
//
//    @Transactional
//    public TrainingSessionResponseDTO updateTrainingSessionStatus(Long id, TrainingSession.TrainingStatus status) {
//        TrainingSession session = trainingSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
//
//        session.setStatus(status);
//
//        // If session is completed, process attendance records and generate certificates
//        if (status == TrainingSession.TrainingStatus.COMPLETED) {
//            processCompletedTrainingSession(session);
//        }
//
//        TrainingSession updatedSession = trainingSessionRepository.save(session);
//        return mapToTrainingSessionDTO(updatedSession);
//    }
//
//    // Training Record methods
//    public List<TrainingRecordResponseDTO> getAllTrainingRecords() {
//        return trainingRecordRepository.findAll().stream()
//                .map(this::mapToTrainingRecordDTO)
//                .collect(Collectors.toList());
//    }
//
//    public TrainingRecordResponseDTO getTrainingRecordById(Long id) {
//        TrainingRecord record = trainingRecordRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + id));
//        return mapToTrainingRecordDTO(record);
//    }
//
//    @Transactional
//    public TrainingRecordResponseDTO createTrainingRecord(TrainingRecordCreateDTO dto) {
//        PersonRegistration personRegistration = personRegistrationRepository.findById(dto.getPersonRegistrationId())
//                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + dto.getPersonRegistrationId()));
//
//        // Check if person already has a training record
//        if (personRegistration.getTrainingRecord() != null) {
//            throw new IllegalStateException("Person already has a training record");
//        }
//
//        TrainingRecord record = TrainingRecord.builder()
//                .personRegistration(personRegistration)
//                .enrolledAt(LocalDateTime.now())
//                .trainingStatus(TrainingRecord.TrainingStatus.ENROLLED)
//                .trainerNotes(dto.getTrainerNotes())
//                .build();
//
//        TrainingRecord savedRecord = trainingRecordRepository.save(record);
//
//        // Update person registration status
//        personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.IN_TRAINING);
//        personRegistration.setTrainingRecord(savedRecord);
//        personRegistrationRepository.save(personRegistration);
//
//        return mapToTrainingRecordDTO(savedRecord);
//    }
//
//    // Training Attendance methods
//    @Transactional
//    public List<TrainingAttendanceResponseDTO> getAllTrainingAttendances() {
//        return trainingAttendanceRepository.findAll().stream()
//                .map(this::mapToTrainingAttendanceDTO)
//                .collect(Collectors.toList());
//    }
//
//    public TrainingAttendanceResponseDTO getTrainingAttendanceById(Long id) {
//        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
//        return mapToTrainingAttendanceDTO(attendance);
//    }
//
//    @Transactional
//    public TrainingAttendanceResponseDTO createTrainingAttendance(TrainingAttendanceCreateDTO dto) {
//        TrainingSession session = trainingSessionRepository.findById(dto.getTrainingSessionId())
//                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + dto.getTrainingSessionId()));
//
//        // Change this line to use personRegistrationId instead of trainingRecordId
//        PersonRegistration personRegistration = personRegistrationRepository.findById(dto.getPersonRegistrationId())
//                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + dto.getPersonRegistrationId()));
//
//        // Find or create training record for this person
//        TrainingRecord record = trainingRecordRepository.findByPersonRegistration(personRegistration)
//                .orElseGet(() -> {
//                    TrainingRecord newRecord = TrainingRecord.builder()
//                            .personRegistration(personRegistration)
//                            .enrolledAt(LocalDateTime.now())
//                            .trainingStatus(TrainingRecord.TrainingStatus.ENROLLED)
//                            .build();
//                    return trainingRecordRepository.save(newRecord);
//                });
//
//        // Check if already registered for this session
//        if (trainingAttendanceRepository.existsByTrainingSessionAndPersonRegistration(session, personRegistration)) {
//            throw new IllegalStateException("Person is already registered for this training session");
//        }
//
//        // Check if session has space
//        long currentParticipants = trainingAttendanceRepository.countByTrainingSession(session);
//        if (session.getMaxParticipants() != null && currentParticipants >= session.getMaxParticipants()) {
//            throw new IllegalStateException("Training session is full");
//        }
//
//        TrainingAttendance attendance = TrainingAttendance.builder()
//                .trainingSession(session)
//                .personRegistration(personRegistration)
//                .trainingRecord(record)
//                .registeredAt(LocalDateTime.now())
//                .attended(false)
//                .notes(dto.getNotes())
//                .build();
//
//        TrainingAttendance savedAttendance = trainingAttendanceRepository.save(attendance);
//
//        // Update training record status if it's still in ENROLLED state
//        if (record.getTrainingStatus() == TrainingRecord.TrainingStatus.ENROLLED) {
//            record.setTrainingStatus(TrainingRecord.TrainingStatus.IN_PROGRESS);
//            trainingRecordRepository.save(record);
//        }
//
//        return mapToTrainingAttendanceDTO(savedAttendance);
//    }
//
//
//
//    @Transactional
//    public TrainingAttendanceResponseDTO checkInAttendance(Long id) {
//        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
//
//        attendance.setAttended(true);
//        attendance.setCheckInTime(LocalDateTime.now());
//
//        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
//        return mapToTrainingAttendanceDTO(updatedAttendance);
//    }
//
//    @Transactional
//    public TrainingAttendanceResponseDTO checkOutAttendance(Long id) {
//        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
//
//        if (attendance.getCheckInTime() == null) {
//            throw new IllegalStateException("Cannot check out without checking in first");
//        }
//
//        attendance.setCheckOutTime(LocalDateTime.now());
//
//        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
//        return mapToTrainingAttendanceDTO(updatedAttendance);
//    }
//
//    @Transactional
//    public TrainingAttendanceResponseDTO updateAttendanceScore(Long id, Integer score, Boolean passed) {
//        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
//
//        attendance.setScore(score);
//        attendance.setPassed(passed);
//
//        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
//        return mapToTrainingAttendanceDTO(updatedAttendance);
//    }
//
//    // Training Certification methods
//    @Transactional
//    public List<TrainingCertificationResponseDTO> getAllTrainingCertifications() {
//        return trainingCertificationRepository.findAll().stream()
//                .map(this::mapToTrainingCertificationDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public TrainingCertificationResponseDTO getTrainingCertificationById(Long id) {
//        TrainingCertification certification = trainingCertificationRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training certification not found with id: " + id));
//        return mapToTrainingCertificationDTO(certification);
//    }
//
//    @Transactional
//    public List<TrainingCertificationResponseDTO> getTrainingCertificationsByTrainingRecord(Long trainingRecordId) {
//        TrainingRecord record = trainingRecordRepository.findById(trainingRecordId)
//                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + trainingRecordId));
//
//        return trainingCertificationRepository.findByTrainingRecord(record).stream()
//                .map(this::mapToTrainingCertificationDTO)
//                .collect(Collectors.toList());
//    }
//
//    // Helper methods
//    @Transactional
//    protected void processCompletedTrainingSession(TrainingSession session) {
//        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingSession(session);
//
//        for (TrainingAttendance attendance : attendances) {
//            // Skip if score is already set
//            if (attendance.getScore() != null) {
//                continue;
//            }
//
//            // Mark as not passed if didn't attend
//            if (attendance.getAttended() == null || !attendance.getAttended()) {
//                attendance.setPassed(false);
//                attendance.setScore(0);
//                trainingAttendanceRepository.save(attendance);
//                continue;
//            }
//
//            // For those who attended but don't have a score yet, set a default passing score
//            // In a real system, this would be based on actual assessment
//            attendance.setPassed(true);
//            attendance.setScore(80); // Default passing score
//            trainingAttendanceRepository.save(attendance);
//
//            // Generate certificates for those who passed
//            if (attendance.getPassed()) {
//                generateCertificatesForAttendance(attendance);
//            }
//
//            // Update training record status
//            updateTrainingRecordStatus(attendance.getTrainingRecord());
//        }
//    }
//
//    @Transactional
//    protected void generateCertificatesForAttendance(TrainingAttendance attendance) {
//        TrainingSession session = attendance.getTrainingSession();
//        TrainingRecord record = attendance.getTrainingRecord();
//
//        // Generate certificates for each certification associated with the training session
//        for (Certification certification : session.getCertifications()) {
//            // Check if certificate already exists
//            if (trainingCertificationRepository.existsByTrainingRecordAndCertification(record, certification)) {
//                continue;
//            }
//
//            // Generate certificate number
//            String certificateNumber = generateCertificateNumber(certification, record);
//
//            // Calculate expiry date (typically 1-3 years from now)
//            LocalDate issuedDate = LocalDate.now();
//            // Change this line to use validityPeriodMonths instead of validityYears
//            LocalDate expiryDate = issuedDate.plusMonths(certification.getValidityPeriodMonths() != null ?
//                    certification.getValidityPeriodMonths() : 24); // Default to 24 months (2 years)
//
//            TrainingCertification trainingCertification = TrainingCertification.builder()
//                    .trainingRecord(record)
//                    .certification(certification)
//                    .issuedDate(issuedDate)
//                    .expiryDate(expiryDate)
//                    .certificateNumber(certificateNumber)
//                    .build();
//
//            trainingCertificationRepository.save(trainingCertification);
//        }
//    }
//
//
//    @Transactional
//    protected void updateTrainingRecordStatus(TrainingRecord record) {
//        // Get all attendances for this record
//        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingRecord(record);
//
//        // If no attendances, do nothing
//        if (attendances.isEmpty()) {
//            return;
//        }
//
//        // Calculate overall score
//        int totalScore = 0;
//        int passedCount = 0;
//        int totalCount = attendances.size();
//
//        for (TrainingAttendance attendance : attendances) {
//            if (attendance.getScore() != null) {
//                totalScore += attendance.getScore();
//            }
//
//            if (attendance.getPassed() != null && attendance.getPassed()) {
//                passedCount++;
//            }
//        }
//
//        int overallScore = totalCount > 0 ? totalScore / totalCount : 0;
//        boolean passed = passedCount >= (totalCount * 0.7); // Pass if 70% of sessions are passed
//
//        // Update training record
//        record.setOverallScore(overallScore);
//        record.setPassed(passed);
//
//        // Update status
//        if (passed) {
//            record.setTrainingStatus(TrainingRecord.TrainingStatus.COMPLETED_PASSED);
//            record.setCompletedAt(LocalDateTime.now());
//
//            // Update person registration status
//            PersonRegistration personRegistration = record.getPersonRegistration();
//            personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD);
//            personRegistrationRepository.save(personRegistration);
//        } else {
//            record.setTrainingStatus(TrainingRecord.TrainingStatus.COMPLETED_FAILED);
//            record.setCompletedAt(LocalDateTime.now());
//
//            // Update person registration status
//            PersonRegistration personRegistration = record.getPersonRegistration();
//            personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.TRAINING_FAILED);
//            personRegistrationRepository.save(personRegistration);
//        }
//
//        trainingRecordRepository.save(record);
//    }
//
//    @Transactional
//    protected String generateCertificateNumber(Certification certification, TrainingRecord record) {
//        // Format: CERT-[CertificationCode]-[Year]-[Random UUID]
//        String certCode = certification.getCode() != null ? certification.getCode() : "SEC";
//        String year = String.valueOf(LocalDate.now().getYear());
//        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//
//        return String.format("CERT-%s-%s-%s", certCode, year, uuid);
//    }
//
//    // TrainingAttendanceController methods
//    @Transactional
//    public List<TrainingAttendanceResponseDTO> getAllAttendances() {
//        return trainingAttendanceRepository.findAll().stream()
//                .map(this::mapToTrainingAttendanceDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public TrainingAttendanceResponseDTO getAttendanceById(Long id) {
//        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
//        return mapToTrainingAttendanceDTO(attendance);
//    }
//
//    @Transactional
//    public TrainingAttendanceResponseDTO updateAttendance(Long id, TrainingAttendanceUpdateDTO dto) {
//        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
//
//        if (dto.getAttended() != null) {
//            attendance.setAttended(dto.getAttended());
//        }
//        if (dto.getNotes() != null) {
//            attendance.setNotes(dto.getNotes());
//        }
//        if (dto.getScore() != null) {
//            attendance.setScore(dto.getScore());
//        }
//        if (dto.getPassed() != null) {
//            attendance.setPassed(dto.getPassed());
//        }
//
//        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
//        return mapToTrainingAttendanceDTO(updatedAttendance);
//    }
//
//    @Transactional
//    public void deleteAttendance(Long id) {
//        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
//        trainingAttendanceRepository.delete(attendance);
//    }
//
//    @Transactional(readOnly = true)
//    public List<TrainingAttendanceResponseDTO> getAttendancesBySession(Long sessionId) {
//        System.out.println("DEBUG: getAttendancesBySession called with sessionId: " + sessionId);
//        try {
//            TrainingSession session = trainingSessionRepository.findById(sessionId)
//                    .orElseThrow(() -> {
//                        System.out.println("ERROR: Training session not found with id: " + sessionId);
//                        return new ResourceNotFoundException("Training session not found with id: " + sessionId);
//                    });
//
//            System.out.println("DEBUG: Found training session: " + session.getTitle());
//
//            List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingSession(session);
//            System.out.println("DEBUG: Found " + attendances.size() + " attendance records");
//
//            List<TrainingAttendanceResponseDTO> result = attendances.stream()
//                    .map(attendance -> {
//                        try {
//                            System.out.println("DEBUG: Mapping attendance ID: " + attendance.getId());
//                            return mapToTrainingAttendanceDTO(attendance);
//                        } catch (Exception e) {
//                            System.out.println("ERROR: Failed to map attendance ID: " + attendance.getId());
//                            e.printStackTrace();
//                            return null;
//                        }
//                    })
//                    .filter(dto -> dto != null)
//                    .collect(Collectors.toList());
//
//            System.out.println("DEBUG: Successfully mapped " + result.size() + " attendance records");
//            return result;
//        } catch (ResourceNotFoundException e) {
//            // Re-throw the exception as it's already logged
//            throw e;
//        } catch (Exception e) {
//            System.out.println("ERROR: Unexpected exception in getAttendancesBySession: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//    }
//
//
//    @Transactional
//    public List<TrainingAttendanceResponseDTO> getAttendancesByPerson(Long personId) {
//        PersonRegistration person = personRegistrationRepository.findById(personId)
//                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
//        return trainingAttendanceRepository.findByPersonRegistration(person).stream()
//                .map(this::mapToTrainingAttendanceDTO)
//                .collect(Collectors.toList());
//    }
//
//    // TrainingCertificationController methods
//    @Transactional
//    public List<TrainingCertificationResponseDTO> getTrainingCertificationsByRecord(Long recordId) {
//        TrainingRecord record = trainingRecordRepository.findById(recordId)
//                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + recordId));
//        return trainingCertificationRepository.findByTrainingRecord(record).stream()
//                .map(this::mapToTrainingCertificationDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public List<TrainingCertificationResponseDTO> getTrainingCertificationsByPerson(Long personId) {
//        PersonRegistration person = personRegistrationRepository.findById(personId)
//                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
//        TrainingRecord record = trainingRecordRepository.findByPersonRegistration(person)
//                .orElseThrow(() -> new ResourceNotFoundException("Training record not found for person with id: " + personId));
//        return trainingCertificationRepository.findByTrainingRecord(record).stream()
//                .map(this::mapToTrainingCertificationDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public List<TrainingCertificationResponseDTO> getTrainingCertificationsByCertification(Long certificationId) {
//        Certification certification = certificationRepository.findById(certificationId)
//                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certificationId));
//        return trainingCertificationRepository.findByCertification(certification).stream()
//                .map(this::mapToTrainingCertificationDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public TrainingCertificationResponseDTO verifyCertificate(String certificateNumber) {
//        TrainingCertification certification = trainingCertificationRepository.findByCertificateNumber(certificateNumber)
//                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with number: " + certificateNumber));
//        return mapToTrainingCertificationDTO(certification);
//    }
//
//    @Transactional
//    public void deleteTrainingCertification(Long id) {
//        TrainingCertification certification = trainingCertificationRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training certification not found with id: " + id));
//        trainingCertificationRepository.delete(certification);
//    }
//
//    // TrainingRecordController methods
//    @Transactional
//    public TrainingRecordResponseDTO updateTrainingRecord(Long id, TrainingRecordUpdateDTO dto) {
//        TrainingRecord record = trainingRecordRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + id));
//
//        if (dto.getTrainerNotes() != null) {
//            record.setTrainerNotes(dto.getTrainerNotes());
//        }
//        if (dto.getTrainingStatus() != null) {
//            record.setTrainingStatus(dto.getTrainingStatus());
//        }
//
//        TrainingRecord updatedRecord = trainingRecordRepository.save(record);
//        return mapToTrainingRecordDTO(updatedRecord);
//    }
//
//    @Transactional
//    public TrainingRecordResponseDTO getTrainingRecordByPerson(Long personId) {
//        PersonRegistration person = personRegistrationRepository.findById(personId)
//                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
//        TrainingRecord record = trainingRecordRepository.findByPersonRegistration(person)
//                .orElseThrow(() -> new ResourceNotFoundException("Training record not found for person with id: " + personId));
//        return mapToTrainingRecordDTO(record);
//    }
//
//    @Transactional
//    public List<TrainingRecordResponseDTO> getTrainingRecordsByStatus(TrainingRecord.TrainingStatus status) {
//        return trainingRecordRepository.findByTrainingStatus(status).stream()
//                .map(this::mapToTrainingRecordDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public TrainingRecordResponseDTO completeTrainingRecord(Long id, Boolean passed) {
//        TrainingRecord record = trainingRecordRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + id));
//
//        record.setCompletedAt(LocalDateTime.now());
//        record.setPassed(passed != null ? passed : false);
//
//        if (passed != null && passed) {
//            record.setTrainingStatus(TrainingRecord.TrainingStatus.COMPLETED_PASSED);
//            // Update person registration status
//            PersonRegistration personRegistration = record.getPersonRegistration();
//            personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD);
//            personRegistrationRepository.save(personRegistration);
//        } else {
//            record.setTrainingStatus(TrainingRecord.TrainingStatus.COMPLETED_FAILED);
//            // Update person registration status
//            PersonRegistration personRegistration = record.getPersonRegistration();
//            personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.TRAINING_FAILED);
//            personRegistrationRepository.save(personRegistration);
//        }
//
//        TrainingRecord updatedRecord = trainingRecordRepository.save(record);
//        return mapToTrainingRecordDTO(updatedRecord);
//    }
//
//    @Transactional
//    public TrainingRecordResponseDTO issueCertifications(Long id) {
//        TrainingRecord record = trainingRecordRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + id));
//
//        // Get all attended sessions for this record
//        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingRecord(record).stream()
//                .filter(a -> a.getAttended() != null && a.getAttended() && a.getPassed() != null && a.getPassed())
//                .collect(Collectors.toList());
//
//        // Generate certificates for each certification associated with the attended sessions
//        for (TrainingAttendance attendance : attendances) {
//            TrainingSession session = attendance.getTrainingSession();
//            for (Certification certification : session.getCertifications()) {
//                // Check if certificate already exists
//                if (trainingCertificationRepository.existsByTrainingRecordAndCertification(record, certification)) {
//                    continue;
//                }
//
//                // Generate certificate
//                String certificateNumber = generateCertificateNumber(certification, record);
//                LocalDate issuedDate = LocalDate.now();
//                LocalDate expiryDate = issuedDate.plusMonths(certification.getValidityPeriodMonths() != null ?
//                        certification.getValidityPeriodMonths() : 24);
//
//                TrainingCertification trainingCertification = TrainingCertification.builder()
//                        .trainingRecord(record)
//                        .certification(certification)
//                        .issuedDate(issuedDate)
//                        .expiryDate(expiryDate)
//                        .certificateNumber(certificateNumber)
//                        .build();
//
//                trainingCertificationRepository.save(trainingCertification);
//            }
//        }
//
//        return mapToTrainingRecordDTO(record);
//    }
//
//    // TrainingSessionController methods
//    @Transactional
//    public TrainingSessionResponseDTO updateTrainingSession(Long id, TrainingSessionCreateDTO dto) {
//        TrainingSession session = trainingSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
//
//        User trainer = userRepository.findById(dto.getTrainerId())
//                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + dto.getTrainerId()));
//
//        session.setTitle(dto.getTitle());
//        session.setDescription(dto.getDescription());
//        session.setTrainer(trainer);
//        session.setStartTime(dto.getStartTime());
//        session.setEndTime(dto.getEndTime());
//        session.setLocation(dto.getLocation());
//        session.setMaxParticipants(dto.getMaxParticipants());
//
//        // Update certifications
//        session.getCertifications().clear();
//        if (dto.getCertificationIds() != null && !dto.getCertificationIds().isEmpty()) {
//            dto.getCertificationIds().forEach(certId -> {
//                Certification cert = certificationRepository.findById(certId)
//                        .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certId));
//                session.getCertifications().add(cert);
//            });
//        }
//
//        TrainingSession updatedSession = trainingSessionRepository.save(session);
//        return mapToTrainingSessionDTO(updatedSession);
//    }
//
//    public void deleteTrainingSession(Long id) {
//        TrainingSession session = trainingSessionRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
//
//        // Check if there are attendances for this session
//        if (!session.getAttendances().isEmpty()) {
//            throw new IllegalStateException("Cannot delete training session with existing attendances");
//        }
//
//        trainingSessionRepository.delete(session);
//    }
//
//    @Transactional
//    public List<TrainingSessionResponseDTO> getUpcomingTrainingSessions() {
//        LocalDateTime now = LocalDateTime.now();
//        return trainingSessionRepository.findByStartTimeAfter(now).stream()
//                .map(this::mapToTrainingSessionDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public List<TrainingSessionResponseDTO> getTrainingSessionsByTrainer(Long trainerId) {
//        User trainer = userRepository.findById(trainerId)
//                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + trainerId));
//        return trainingSessionRepository.findByTrainer(trainer).stream()
//                .map(this::mapToTrainingSessionDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public List<TrainingSessionResponseDTO> getTrainingSessionsByStatus(TrainingSession.TrainingStatus status) {
//        return trainingSessionRepository.findByStatus(status).stream()
//                .map(this::mapToTrainingSessionDTO)
//                .collect(Collectors.toList());
//    }
//
//
//    // Mapping methods
//    @Transactional
//    protected TrainingSessionResponseDTO mapToTrainingSessionDTO(TrainingSession session) {
//        List<TrainingAttendanceResponseDTO> attendances = session.getAttendances().stream()
//                .map(this::mapToTrainingAttendanceDTO)
//                .collect(Collectors.toList());
//
//        List<CertificationSummaryDTO> certifications = session.getCertifications().stream()
//                .map(cert -> CertificationSummaryDTO.builder()
//                        .id(cert.getId())
//                        .name(cert.getName())
//                        .description(cert.getDescription())
//                        .issuingAuthority(cert.getIssuingAuthority())
//                        .build())
//                .collect(Collectors.toList());
//
//        return TrainingSessionResponseDTO.builder()
//                .id(session.getId())
//                .title(session.getTitle())
//                .description(session.getDescription())
//                .trainerId(session.getTrainer() != null ? session.getTrainer().getId() : null)
//                .trainerName(session.getTrainer() != null ?
//                        session.getTrainer().getFirstName() + " " + session.getTrainer().getLastName() : null)
//                .startTime(session.getStartTime())
//                .endTime(session.getEndTime())
//                .location(session.getLocation())
//                .maxParticipants(session.getMaxParticipants())
//                .status(session.getStatus())
//                .createdAt(session.getCreatedAt())
//                .updatedAt(session.getUpdatedAt())
//                .certifications(certifications)
//                .attendances(attendances)
//                .currentParticipants(attendances.size())
//                .availableSlots(session.getMaxParticipants() != null ?
//                        session.getMaxParticipants() - attendances.size() : 0)
//                .isFull(session.getMaxParticipants() != null &&
//                        attendances.size() >= session.getMaxParticipants())
//                .durationInHours(session.getStartTime() != null && session.getEndTime() != null ?
//                        java.time.Duration.between(session.getStartTime(), session.getEndTime()).toHours() : 0)
//                .build();
//    }
//
//
////    @Transactional
////    protected TrainingAttendanceResponseDTO mapToTrainingAttendanceDTO(TrainingAttendance attendance) {
////        return TrainingAttendanceResponseDTO.builder()
////                .id(attendance.getId())
////                .trainingSessionId(attendance.getTrainingSession() != null ?
////                        attendance.getTrainingSession().getId() : null)
////                .trainingSessionTitle(attendance.getTrainingSession() != null ?
////                        attendance.getTrainingSession().getTitle() : null)
////                .trainingRecordId(attendance.getTrainingRecord() != null ?
////                        attendance.getTrainingRecord().getId() : null)
////                .participantName(attendance.getPersonRegistration() != null ?
////                        attendance.getPersonRegistration().getFirstName() + " " +
////                                attendance.getPersonRegistration().getLastName() : null)
////                .registeredAt(attendance.getRegisteredAt())
////                .attended(attendance.getAttended())
////                .checkInTime(attendance.getCheckInTime())
////                .checkOutTime(attendance.getCheckOutTime())
////                .passed(attendance.getPassed())
////                .score(attendance.getScore())
////                .notes(attendance.getNotes())
////                .createdAt(attendance.getCreatedAt())
////                .updatedAt(attendance.getUpdatedAt())
////                .sessionStartTime(attendance.getTrainingSession() != null ?
////                        attendance.getTrainingSession().getStartTime() : null)
////                .sessionEndTime(attendance.getTrainingSession() != null ?
////                        attendance.getTrainingSession().getEndTime() : null)
////                .sessionLocation(attendance.getTrainingSession() != null ?
////                        attendance.getTrainingSession().getLocation() : null)
////                .trainerName(attendance.getTrainingSession() != null &&
////                        attendance.getTrainingSession().getTrainer() != null ?
////                        attendance.getTrainingSession().getTrainer().getFirstName() + " " +
////                                attendance.getTrainingSession().getTrainer().getLastName() : null)
////                .build();
////    }
//
//    @Transactional
//    protected TrainingAttendanceResponseDTO mapToTrainingAttendanceDTO(TrainingAttendance attendance) {
//        System.out.println("DEBUG: Starting to map attendance ID: " + attendance.getId());
//        try {
//            // Log each property access to find potential NPEs
//            String sessionId = "null";
//            String sessionTitle = "null";
//            String recordId = "null";
//            String participantName = "null";
//
//            if (attendance.getTrainingSession() != null) {
//                sessionId = String.valueOf(attendance.getTrainingSession().getId());
//                sessionTitle = attendance.getTrainingSession().getTitle();
//                System.out.println("DEBUG: Session info - ID: " + sessionId + ", Title: " + sessionTitle);
//            } else {
//                System.out.println("WARNING: TrainingSession is null for attendance ID: " + attendance.getId());
//            }
//
//            if (attendance.getTrainingRecord() != null) {
//                recordId = String.valueOf(attendance.getTrainingRecord().getId());
//                System.out.println("DEBUG: TrainingRecord ID: " + recordId);
//            } else {
//                System.out.println("WARNING: TrainingRecord is null for attendance ID: " + attendance.getId());
//            }
//
//            if (attendance.getPersonRegistration() != null) {
//                participantName = attendance.getPersonRegistration().getFirstName() + " " +
//                        attendance.getPersonRegistration().getLastName();
//                System.out.println("DEBUG: Participant name: " + participantName);
//            } else {
//                System.out.println("WARNING: PersonRegistration is null for attendance ID: " + attendance.getId());
//            }
//
//            String trainerName = "null";
//            String sessionLocation = "null";
//            if (attendance.getTrainingSession() != null) {
//                if (attendance.getTrainingSession().getTrainer() != null) {
//                    trainerName = attendance.getTrainingSession().getTrainer().getFirstName() + " " +
//                            attendance.getTrainingSession().getTrainer().getLastName();
//                    System.out.println("DEBUG: Trainer name: " + trainerName);
//                } else {
//                    System.out.println("WARNING: Trainer is null for session ID: " + sessionId);
//                }
//
//                sessionLocation = attendance.getTrainingSession().getLocation();
//                System.out.println("DEBUG: Session location: " + sessionLocation);
//            }
//
//            TrainingAttendanceResponseDTO dto = TrainingAttendanceResponseDTO.builder()
//                    .id(attendance.getId())
//                    .trainingSessionId(attendance.getTrainingSession() != null ?
//                            attendance.getTrainingSession().getId() : null)
//                    .trainingSessionTitle(attendance.getTrainingSession() != null ?
//                            attendance.getTrainingSession().getTitle() : null)
//                    .trainingRecordId(attendance.getTrainingRecord() != null ?
//                            attendance.getTrainingRecord().getId() : null)
//                    .participantName(attendance.getPersonRegistration() != null ?
//                            attendance.getPersonRegistration().getFirstName() + " " +
//                                    attendance.getPersonRegistration().getLastName() : null)
//                    .registeredAt(attendance.getRegisteredAt())
//                    .attended(attendance.getAttended())
//                    .checkInTime(attendance.getCheckInTime())
//                    .checkOutTime(attendance.getCheckOutTime())
//                    .passed(attendance.getPassed())
//                    .score(attendance.getScore())
//                    .notes(attendance.getNotes())
//                    .createdAt(attendance.getCreatedAt())
//                    .updatedAt(attendance.getUpdatedAt())
//                    .sessionStartTime(attendance.getTrainingSession() != null ?
//                            attendance.getTrainingSession().getStartTime() : null)
//                    .sessionEndTime(attendance.getTrainingSession() != null ?
//                            attendance.getTrainingSession().getEndTime() : null)
//                    .sessionLocation(attendance.getTrainingSession() != null ?
//                            attendance.getTrainingSession().getLocation() : null)
//                    .trainerName(attendance.getTrainingSession() != null &&
//                            attendance.getTrainingSession().getTrainer() != null ?
//                            attendance.getTrainingSession().getTrainer().getFirstName() + " " +
//                                    attendance.getTrainingSession().getTrainer().getLastName() : null)
//                    .build();
//
//            System.out.println("DEBUG: Successfully mapped attendance ID: " + attendance.getId());
//            return dto;
//        } catch (Exception e) {
//            System.out.println("ERROR: Exception while mapping attendance ID: " + attendance.getId());
//            System.out.println("ERROR: Exception message: " + e.getMessage());
//            e.printStackTrace();
//            throw e;
//        }
//    }
//
//
//    @Transactional
//    protected TrainingRecordResponseDTO mapToTrainingRecordDTO(TrainingRecord record) {
//        List<TrainingAttendanceResponseDTO> attendances = record.getAttendances().stream()
//                .map(this::mapToTrainingAttendanceDTO)
//                .collect(Collectors.toList());
//
//        List<TrainingCertificationResponseDTO> certifications = record.getCertifications().stream()
//                .map(this::mapToTrainingCertificationDTO)
//                .collect(Collectors.toList());
//
//        int totalSessionsAttended = (int) attendances.stream()
//                .filter(a -> a.getAttended() != null && a.getAttended())
//                .count();
//
//        int totalSessionsRequired = attendances.size();
//        double attendancePercentage = totalSessionsRequired > 0 ?
//                (double) totalSessionsAttended / totalSessionsRequired * 100 : 0;
//
//        return TrainingRecordResponseDTO.builder()
//                .id(record.getId())
//                .personRegistrationId(record.getPersonRegistration() != null ?
//                        record.getPersonRegistration().getId() : null)
//                .personName(record.getPersonRegistration() != null ?
//                        record.getPersonRegistration().getFirstName() + " " +
//                                record.getPersonRegistration().getLastName() : null)
//                .enrolledAt(record.getEnrolledAt())
//                .completedAt(record.getCompletedAt())
//                .overallScore(record.getOverallScore())
//                .passed(record.getPassed())
//                .trainingStatus(record.getTrainingStatus())
//                .trainerNotes(record.getTrainerNotes())
//                .createdAt(record.getCreatedAt())
//                .updatedAt(record.getUpdatedAt())
//                .attendances(attendances)
//                .certifications(certifications)
//                .totalSessionsAttended(totalSessionsAttended)
//                .totalSessionsRequired(totalSessionsRequired)
//                .attendancePercentage(attendancePercentage)
//                .build();
//    }
//
//    @Transactional
//    protected TrainingCertificationResponseDTO mapToTrainingCertificationDTO(TrainingCertification certification) {
//        LocalDate now = LocalDate.now();
//        boolean isExpired = certification.getExpiryDate() != null && certification.getExpiryDate().isBefore(now);
//        int daysUntilExpiry = certification.getExpiryDate() != null ?
//                (int) java.time.temporal.ChronoUnit.DAYS.between(now, certification.getExpiryDate()) : 0;
//
//        return TrainingCertificationResponseDTO.builder()
//                .id(certification.getId())
//                .trainingRecordId(certification.getTrainingRecord() != null ?
//                        certification.getTrainingRecord().getId() : null)
//                .participantName(certification.getTrainingRecord() != null &&
//                        certification.getTrainingRecord().getPersonRegistration() != null ?
//                        certification.getTrainingRecord().getPersonRegistration().getFirstName() + " " +
//                                certification.getTrainingRecord().getPersonRegistration().getLastName() : null)
//                .certificationId(certification.getCertification() != null ?
//                        certification.getCertification().getId() : null)
//                .certificationName(certification.getCertification() != null ?
//                        certification.getCertification().getName() : null)
//                .certificationDescription(certification.getCertification() != null ?
//                        certification.getCertification().getDescription() : null)
//                .issuingAuthority(certification.getCertification() != null ?
//                        certification.getCertification().getIssuingAuthority() : null)
//                .issuedDate(certification.getIssuedDate())
//                .expiryDate(certification.getExpiryDate())
//                .certificateNumber(certification.getCertificateNumber())
//                .isExpired(isExpired)
//                .daysUntilExpiry(daysUntilExpiry)
//                .createdAt(certification.getCreatedAt())
//                .build();
//    }
//}

package com.dep.soms.service;

import com.dep.soms.dto.person.*;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.*;
import com.dep.soms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingService {
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingAttendanceRepository trainingAttendanceRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    private final PersonRegistrationRepository personRegistrationRepository;
    private final CertificationRepository certificationRepository;
    private final TrainingCertificationRepository trainingCertificationRepository;
    private final UserRepository userRepository;

    // Training Session methods
    @Transactional
    public List<TrainingSessionResponseDTO> getAllTrainingSessions() {
        return trainingSessionRepository.findAll().stream()
                .map(this::mapToTrainingSessionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingSessionResponseDTO getTrainingSessionById(Long id) {
        TrainingSession session = trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));
        return mapToTrainingSessionDTO(session);
    }


    @Transactional(readOnly = true)
    public List<TrainingAttendanceResponseDTO> getTrainingSessionAttendance(Long sessionId) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + sessionId));

        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingSession(session);
        return attendances.stream()
                .map(this::mapToTrainingAttendanceDTO)
                .collect(Collectors.toList());
    }

//    @Transactional(readOnly = true)
//    public List<TrainingAttendanceResponseDTO> getTrainingSessionAttendanceByDate(Long sessionId, LocalDate date) {
//        TrainingSession session = trainingSessionRepository.findById(sessionId)
//                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + sessionId));
//
//        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingSessionAndSessionDate(session, date);
//        return attendances.stream()
//                .map(this::mapToTrainingAttendanceDTO)
//                .collect(Collectors.toList());
//    }

    @Transactional(readOnly = true)
    public List<TrainingAttendanceResponseDTO> getTrainingSessionAttendanceByDate(Long sessionId, LocalDate date) {
        System.out.println("DEBUG: [Service] Fetching attendances for sessionId: " + sessionId + " and date: " + date);

        // Use the direct ID query method instead of fetching the session first
        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingSessionIdAndSessionDate(sessionId, date);

        System.out.println("DEBUG: [Service] Found " + attendances.size() + " attendance records");

        return attendances.stream()
                .map(this::mapToTrainingAttendanceDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrainingAttendanceResponseDTO> getAttendancesByDate(LocalDate date, Long sessionId) {
        if (sessionId != null) {
            // Get attendances for specific session and date
            return trainingAttendanceRepository.findByTrainingSessionIdAndSessionDate(sessionId, date)
                    .stream()
                    .map(this::mapToTrainingAttendanceDTO)
                    .collect(Collectors.toList());
        } else {
            // Get all attendances for the date (original behavior)
            List<TrainingSession> sessions = trainingSessionRepository
                    .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);

            return sessions.stream()
                    .flatMap(session -> trainingAttendanceRepository
                            .findByTrainingSessionAndSessionDate(session, date).stream())
                    .map(this::mapToTrainingAttendanceDTO)
                    .collect(Collectors.toList());
        }
    }
    @Transactional
    public TrainingAttendanceResponseDTO updateTrainingAttendance(Long id, TrainingAttendanceUpdateDTO dto) {
        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));

        if (dto.getAttended() != null) {
            attendance.setAttended(dto.getAttended());
        }

        if (dto.getCheckInTime() != null) {
            attendance.setCheckInTime(dto.getCheckInTime());
        }

        if (dto.getCheckOutTime() != null) {
            attendance.setCheckOutTime(dto.getCheckOutTime());
        }

        if (dto.getScore() != null) {
            attendance.setScore(dto.getScore());
        }

        if (dto.getPassed() != null) {
            attendance.setPassed(dto.getPassed());
        }

        if (dto.getNotes() != null) {
            attendance.setNotes(dto.getNotes());
        }

        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
        return mapToTrainingAttendanceDTO(updatedAttendance);
    }

//

    @Transactional(readOnly = true)
    public List<TrainingSessionResponseDTO> getOngoingTrainingSessions() {
        LocalDate today = LocalDate.now();
        List<TrainingSession> sessions = trainingSessionRepository.findByStartDateBeforeAndEndDateAfter(today, today);
        return sessions.stream()
                .map(this::mapToTrainingSessionDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public List<TrainingAttendanceResponseDTO> assignRecruitsToTrainingSession(Long sessionId, List<Long> recruitIds) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + sessionId));

        List<TrainingAttendanceResponseDTO> results = new ArrayList<>();

        for (Long recruitId : recruitIds) {
            PersonRegistration recruit = personRegistrationRepository.findById(recruitId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recruit not found with id: " + recruitId));

            // Check if recruit is already assigned to this session
            if (trainingAttendanceRepository.existsByTrainingSessionAndPersonRegistration(session, recruit)) {
                continue;
            }

            // Find or create training record for this person
            TrainingRecord record = trainingRecordRepository.findByPersonRegistration(recruit)
                    .orElseGet(() -> {
                        TrainingRecord newRecord = TrainingRecord.builder()
                                .personRegistration(recruit)
                                .enrolledAt(LocalDateTime.now())
                                .trainingStatus(TrainingRecord.TrainingStatus.ENROLLED)
                                .build();
                        return trainingRecordRepository.save(newRecord);
                    });

            // Create attendance records for each day of the training session
            List<TrainingAttendance> attendances = new ArrayList<>();
            LocalDate currentDate = session.getStartDate();

            while (!currentDate.isAfter(session.getEndDate())) {
                TrainingAttendance attendance = TrainingAttendance.builder()
                        .trainingSession(session)
                        .personRegistration(recruit)
                        .trainingRecord(record)
                        .registeredAt(LocalDateTime.now())
                        .attended(false)
                        .sessionDate(currentDate)
                        .build();

                attendances.add(trainingAttendanceRepository.save(attendance));
                currentDate = currentDate.plusDays(1);
            }

            // Update recruit status to IN_TRAINING if they're approved for training
            if (recruit.getRegistrationStatus() == PersonRegistration.RegistrationStatus.APPROVED_FOR_TRAINING) {
                recruit.setRegistrationStatus(PersonRegistration.RegistrationStatus.IN_TRAINING);
                personRegistrationRepository.save(recruit);
            }

            // Add the first attendance record to results
            if (!attendances.isEmpty()) {
                results.add(mapToTrainingAttendanceDTO(attendances.get(0)));
            }
        }

        return results;
    }


    @Transactional
    public TrainingSessionResponseDTO createTrainingSession(TrainingSessionCreateDTO dto) {
        User trainer = userRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + dto.getTrainerId()));

        // Validate dates
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Create LocalDateTime values for backward compatibility
        LocalDateTime startTime = dto.getStartDate().atTime(dto.getDailyStartTime());
        LocalDateTime endTime = dto.getEndDate().atTime(dto.getDailyEndTime());

        TrainingSession session = TrainingSession.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .trainer(trainer)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .dailyStartTime(dto.getDailyStartTime())
                .dailyEndTime(dto.getDailyEndTime())
                .startTime(startTime)  // For backward compatibility
                .endTime(endTime)      // For backward compatibility
                .location(dto.getLocation())
                .maxParticipants(dto.getMaxParticipants())
                .status(TrainingSession.TrainingStatus.SCHEDULED)
                .build();

        if (dto.getCertificationIds() != null && !dto.getCertificationIds().isEmpty()) {
            dto.getCertificationIds().forEach(certId -> {
                Certification cert = certificationRepository.findById(certId)
                        .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certId));
                session.getCertifications().add(cert);
            });
        }

        TrainingSession savedSession = trainingSessionRepository.save(session);
        return mapToTrainingSessionDTO(savedSession);
    }


    @Transactional
    public TrainingSessionResponseDTO updateTrainingSessionStatus(Long id, TrainingSession.TrainingStatus status) {
        TrainingSession session = trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));

        session.setStatus(status);

        // If session is completed, process attendance records and generate certificates
        if (status == TrainingSession.TrainingStatus.COMPLETED) {
            processCompletedTrainingSession(session);
        }

        TrainingSession updatedSession = trainingSessionRepository.save(session);
        return mapToTrainingSessionDTO(updatedSession);
    }

    // Training Record methods
    public List<TrainingRecordResponseDTO> getAllTrainingRecords() {
        return trainingRecordRepository.findAll().stream()
                .map(this::mapToTrainingRecordDTO)
                .collect(Collectors.toList());
    }

    public TrainingRecordResponseDTO getTrainingRecordById(Long id) {
        TrainingRecord record = trainingRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + id));
        return mapToTrainingRecordDTO(record);
    }

    @Transactional
    public TrainingRecordResponseDTO createTrainingRecord(TrainingRecordCreateDTO dto) {
        PersonRegistration personRegistration = personRegistrationRepository.findById(dto.getPersonRegistrationId())
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + dto.getPersonRegistrationId()));

        // Check if person already has a training record
        if (personRegistration.getTrainingRecord() != null) {
            throw new IllegalStateException("Person already has a training record");
        }

        TrainingRecord record = TrainingRecord.builder()
                .personRegistration(personRegistration)
                .enrolledAt(LocalDateTime.now())
                .trainingStatus(TrainingRecord.TrainingStatus.ENROLLED)
                .trainerNotes(dto.getTrainerNotes())
                .build();

        TrainingRecord savedRecord = trainingRecordRepository.save(record);

        // Update person registration status
        personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.IN_TRAINING);
        personRegistration.setTrainingRecord(savedRecord);
        personRegistrationRepository.save(personRegistration);

        return mapToTrainingRecordDTO(savedRecord);
    }

    // Training Attendance methods
    @Transactional
    public List<TrainingAttendanceResponseDTO> getAllTrainingAttendances() {
        return trainingAttendanceRepository.findAll().stream()
                .map(this::mapToTrainingAttendanceDTO)
                .collect(Collectors.toList());
    }

    public TrainingAttendanceResponseDTO getTrainingAttendanceById(Long id) {
        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
        return mapToTrainingAttendanceDTO(attendance);
    }


    @Transactional
    public TrainingAttendanceResponseDTO createTrainingAttendance(TrainingAttendanceCreateDTO dto) {
        TrainingSession session = trainingSessionRepository.findById(dto.getTrainingSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + dto.getTrainingSessionId()));

        PersonRegistration personRegistration = personRegistrationRepository.findById(dto.getPersonRegistrationId())
                .orElseThrow(() -> new ResourceNotFoundException("Person registration not found with id: " + dto.getPersonRegistrationId()));

        // Find or create training record for this person
        TrainingRecord record = trainingRecordRepository.findByPersonRegistration(personRegistration)
                .orElseGet(() -> {
                    TrainingRecord newRecord = TrainingRecord.builder()
                            .personRegistration(personRegistration)
                            .enrolledAt(LocalDateTime.now())
                            .trainingStatus(TrainingRecord.TrainingStatus.ENROLLED)
                            .build();
                    return trainingRecordRepository.save(newRecord);
                });

        // Check if already registered for this session
        if (trainingAttendanceRepository.existsByTrainingSessionAndPersonRegistration(session, personRegistration)) {
            throw new IllegalStateException("Person is already registered for this training session");
        }

        // Check if session has space
        long currentParticipants = trainingAttendanceRepository.countByTrainingSession(session);
        if (session.getMaxParticipants() != null && currentParticipants >= session.getMaxParticipants()) {
            throw new IllegalStateException("Training session is full");
        }

        // Create attendance records for each day of the training session
        List<TrainingAttendance> attendances = new ArrayList<>();
        LocalDate currentDate = session.getStartDate();

        while (!currentDate.isAfter(session.getEndDate())) {
            LocalDateTime dayStartTime = currentDate.atTime(session.getDailyStartTime());

            TrainingAttendance attendance = TrainingAttendance.builder()
                    .trainingSession(session)
                    .personRegistration(personRegistration)
                    .trainingRecord(record)
                    .registeredAt(LocalDateTime.now())
                    .attended(false)
                    .notes(dto.getNotes())
                    .sessionDate(currentDate)  // Add this field to TrainingAttendance entity
                    .build();

            attendances.add(trainingAttendanceRepository.save(attendance));
            currentDate = currentDate.plusDays(1);
        }

        // Update training record status if it's still in ENROLLED state
        if (record.getTrainingStatus() == TrainingRecord.TrainingStatus.ENROLLED) {
            record.setTrainingStatus(TrainingRecord.TrainingStatus.IN_PROGRESS);
            trainingRecordRepository.save(record);
        }

        // Return the first attendance record as a response
        return mapToTrainingAttendanceDTO(attendances.get(0));
    }


    @Transactional
    public TrainingAttendanceResponseDTO checkInAttendance(Long id) {
        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));

        attendance.setAttended(true);
        attendance.setCheckInTime(LocalDateTime.now());

        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
        return mapToTrainingAttendanceDTO(updatedAttendance);
    }

    @Transactional
    public TrainingAttendanceResponseDTO checkOutAttendance(Long id) {
        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));

        if (attendance.getCheckInTime() == null) {
            throw new IllegalStateException("Cannot check out without checking in first");
        }

        attendance.setCheckOutTime(LocalDateTime.now());

        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
        return mapToTrainingAttendanceDTO(updatedAttendance);
    }

    @Transactional
    public TrainingAttendanceResponseDTO updateAttendanceScore(Long id, Integer score, Boolean passed) {
        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));

        attendance.setScore(score);
        attendance.setPassed(passed);

        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
        return mapToTrainingAttendanceDTO(updatedAttendance);
    }

    // Training Certification methods
    @Transactional
    public List<TrainingCertificationResponseDTO> getAllTrainingCertifications() {
        return trainingCertificationRepository.findAll().stream()
                .map(this::mapToTrainingCertificationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingCertificationResponseDTO getTrainingCertificationById(Long id) {
        TrainingCertification certification = trainingCertificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training certification not found with id: " + id));
        return mapToTrainingCertificationDTO(certification);
    }

    @Transactional
    public List<TrainingCertificationResponseDTO> getTrainingCertificationsByTrainingRecord(Long trainingRecordId) {
        TrainingRecord record = trainingRecordRepository.findById(trainingRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + trainingRecordId));

        return trainingCertificationRepository.findByTrainingRecord(record).stream()
                .map(this::mapToTrainingCertificationDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    @Transactional
    protected void processCompletedTrainingSession(TrainingSession session) {
        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingSession(session);

        for (TrainingAttendance attendance : attendances) {
            // Skip if score is already set
            if (attendance.getScore() != null) {
                continue;
            }

            // Mark as not passed if didn't attend
            if (attendance.getAttended() == null || !attendance.getAttended()) {
                attendance.setPassed(false);
                attendance.setScore(0);
                trainingAttendanceRepository.save(attendance);
                continue;
            }

            // For those who attended but don't have a score yet, set a default passing score
            // In a real system, this would be based on actual assessment
            attendance.setPassed(true);
            attendance.setScore(80); // Default passing score
            trainingAttendanceRepository.save(attendance);

            // Generate certificates for those who passed
            if (attendance.getPassed()) {
                generateCertificatesForAttendance(attendance);
            }

            // Update training record status
            updateTrainingRecordStatus(attendance.getTrainingRecord());
        }
    }

    @Transactional
    protected void generateCertificatesForAttendance(TrainingAttendance attendance) {
        TrainingSession session = attendance.getTrainingSession();
        TrainingRecord record = attendance.getTrainingRecord();

        // Generate certificates for each certification associated with the training session
        for (Certification certification : session.getCertifications()) {
            // Check if certificate already exists
            if (trainingCertificationRepository.existsByTrainingRecordAndCertification(record, certification)) {
                continue;
            }

            // Generate certificate number
            String certificateNumber = generateCertificateNumber(certification, record);

            // Calculate expiry date (typically 1-3 years from now)
            LocalDate issuedDate = LocalDate.now();
            // Change this line to use validityPeriodMonths instead of validityYears
            LocalDate expiryDate = issuedDate.plusMonths(certification.getValidityPeriodMonths() != null ?
                    certification.getValidityPeriodMonths() : 24); // Default to 24 months (2 years)

            TrainingCertification trainingCertification = TrainingCertification.builder()
                    .trainingRecord(record)
                    .certification(certification)
                    .issuedDate(issuedDate)
                    .expiryDate(expiryDate)
                    .certificateNumber(certificateNumber)
                    .build();

            trainingCertificationRepository.save(trainingCertification);
        }
    }


    @Transactional
    protected void updateTrainingRecordStatus(TrainingRecord record) {
        // Get all attendances for this record
        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingRecord(record);

        // If no attendances, do nothing
        if (attendances.isEmpty()) {
            return;
        }

        // Calculate overall score
        int totalScore = 0;
        int passedCount = 0;
        int totalCount = attendances.size();

        for (TrainingAttendance attendance : attendances) {
            if (attendance.getScore() != null) {
                totalScore += attendance.getScore();
            }

            if (attendance.getPassed() != null && attendance.getPassed()) {
                passedCount++;
            }
        }

        int overallScore = totalCount > 0 ? totalScore / totalCount : 0;
        boolean passed = passedCount >= (totalCount * 0.7); // Pass if 70% of sessions are passed

        // Update training record
        record.setOverallScore(overallScore);
        record.setPassed(passed);

        // Update status
        if (passed) {
            record.setTrainingStatus(TrainingRecord.TrainingStatus.COMPLETED_PASSED);
            record.setCompletedAt(LocalDateTime.now());

            // Update person registration status
            PersonRegistration personRegistration = record.getPersonRegistration();
            personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD);
            personRegistrationRepository.save(personRegistration);
        } else {
            record.setTrainingStatus(TrainingRecord.TrainingStatus.COMPLETED_FAILED);
            record.setCompletedAt(LocalDateTime.now());

            // Update person registration status
            PersonRegistration personRegistration = record.getPersonRegistration();
            personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.TRAINING_FAILED);
            personRegistrationRepository.save(personRegistration);
        }

        trainingRecordRepository.save(record);
    }

    @Transactional
    protected String generateCertificateNumber(Certification certification, TrainingRecord record) {
        // Format: CERT-[CertificationCode]-[Year]-[Random UUID]
        String certCode = certification.getCode() != null ? certification.getCode() : "SEC";
        String year = String.valueOf(LocalDate.now().getYear());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return String.format("CERT-%s-%s-%s", certCode, year, uuid);
    }

    // TrainingAttendanceController methods
    @Transactional
    public List<TrainingAttendanceResponseDTO> getAllAttendances() {
        return trainingAttendanceRepository.findAll().stream()
                .map(this::mapToTrainingAttendanceDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingAttendanceResponseDTO getAttendanceById(Long id) {
        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
        return mapToTrainingAttendanceDTO(attendance);
    }

    @Transactional
    public TrainingAttendanceResponseDTO updateAttendance(Long id, TrainingAttendanceUpdateDTO dto) {
        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));

        if (dto.getAttended() != null) {
            attendance.setAttended(dto.getAttended());
        }
        if (dto.getNotes() != null) {
            attendance.setNotes(dto.getNotes());
        }
        if (dto.getScore() != null) {
            attendance.setScore(dto.getScore());
        }
        if (dto.getPassed() != null) {
            attendance.setPassed(dto.getPassed());
        }

        TrainingAttendance updatedAttendance = trainingAttendanceRepository.save(attendance);
        return mapToTrainingAttendanceDTO(updatedAttendance);
    }

    @Transactional
    public void deleteAttendance(Long id) {
        TrainingAttendance attendance = trainingAttendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training attendance not found with id: " + id));
        trainingAttendanceRepository.delete(attendance);
    }

    @Transactional(readOnly = true)
    public List<TrainingAttendanceResponseDTO> getAttendancesBySession(Long sessionId) {
        System.out.println("DEBUG: getAttendancesBySession called with sessionId: " + sessionId);
        try {
            TrainingSession session = trainingSessionRepository.findById(sessionId)
                    .orElseThrow(() -> {
                        System.out.println("ERROR: Training session not found with id: " + sessionId);
                        return new ResourceNotFoundException("Training session not found with id: " + sessionId);
                    });

            System.out.println("DEBUG: Found training session: " + session.getTitle());

            List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingSession(session);
            System.out.println("DEBUG: Found " + attendances.size() + " attendance records");

            List<TrainingAttendanceResponseDTO> result = attendances.stream()
                    .map(attendance -> {
                        try {
                            System.out.println("DEBUG: Mapping attendance ID: " + attendance.getId());
                            return mapToTrainingAttendanceDTO(attendance);
                        } catch (Exception e) {
                            System.out.println("ERROR: Failed to map attendance ID: " + attendance.getId());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            System.out.println("DEBUG: Successfully mapped " + result.size() + " attendance records");
            return result;
        } catch (ResourceNotFoundException e) {
            // Re-throw the exception as it's already logged
            throw e;
        } catch (Exception e) {
            System.out.println("ERROR: Unexpected exception in getAttendancesBySession: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    @Transactional
    public List<TrainingAttendanceResponseDTO> getAttendancesByPerson(Long personId) {
        PersonRegistration person = personRegistrationRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
        return trainingAttendanceRepository.findByPersonRegistration(person).stream()
                .map(this::mapToTrainingAttendanceDTO)
                .collect(Collectors.toList());
    }

    // TrainingCertificationController methods
    @Transactional
    public List<TrainingCertificationResponseDTO> getTrainingCertificationsByRecord(Long recordId) {
        TrainingRecord record = trainingRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + recordId));
        return trainingCertificationRepository.findByTrainingRecord(record).stream()
                .map(this::mapToTrainingCertificationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TrainingCertificationResponseDTO> getTrainingCertificationsByPerson(Long personId) {
        PersonRegistration person = personRegistrationRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
        TrainingRecord record = trainingRecordRepository.findByPersonRegistration(person)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found for person with id: " + personId));
        return trainingCertificationRepository.findByTrainingRecord(record).stream()
                .map(this::mapToTrainingCertificationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TrainingCertificationResponseDTO> getTrainingCertificationsByCertification(Long certificationId) {
        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certificationId));
        return trainingCertificationRepository.findByCertification(certification).stream()
                .map(this::mapToTrainingCertificationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingCertificationResponseDTO verifyCertificate(String certificateNumber) {
        TrainingCertification certification = trainingCertificationRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with number: " + certificateNumber));
        return mapToTrainingCertificationDTO(certification);
    }

    @Transactional
    public void deleteTrainingCertification(Long id) {
        TrainingCertification certification = trainingCertificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training certification not found with id: " + id));
        trainingCertificationRepository.delete(certification);
    }

    // TrainingRecordController methods
    @Transactional
    public TrainingRecordResponseDTO updateTrainingRecord(Long id, TrainingRecordUpdateDTO dto) {
        TrainingRecord record = trainingRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + id));

        if (dto.getTrainerNotes() != null) {
            record.setTrainerNotes(dto.getTrainerNotes());
        }
        if (dto.getTrainingStatus() != null) {
            record.setTrainingStatus(dto.getTrainingStatus());
        }

        TrainingRecord updatedRecord = trainingRecordRepository.save(record);
        return mapToTrainingRecordDTO(updatedRecord);
    }

    @Transactional
    public TrainingRecordResponseDTO getTrainingRecordByPerson(Long personId) {
        PersonRegistration person = personRegistrationRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));
        TrainingRecord record = trainingRecordRepository.findByPersonRegistration(person)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found for person with id: " + personId));
        return mapToTrainingRecordDTO(record);
    }

    @Transactional
    public List<TrainingRecordResponseDTO> getTrainingRecordsByStatus(TrainingRecord.TrainingStatus status) {
        return trainingRecordRepository.findByTrainingStatus(status).stream()
                .map(this::mapToTrainingRecordDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainingRecordResponseDTO completeTrainingRecord(Long id, Boolean passed) {
        TrainingRecord record = trainingRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + id));

        record.setCompletedAt(LocalDateTime.now());
        record.setPassed(passed != null ? passed : false);

        if (passed != null && passed) {
            record.setTrainingStatus(TrainingRecord.TrainingStatus.COMPLETED_PASSED);
            // Update person registration status
            PersonRegistration personRegistration = record.getPersonRegistration();
            personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.APPROVED_FOR_GUARD);
            personRegistrationRepository.save(personRegistration);
        } else {
            record.setTrainingStatus(TrainingRecord.TrainingStatus.COMPLETED_FAILED);
            // Update person registration status
            PersonRegistration personRegistration = record.getPersonRegistration();
            personRegistration.setRegistrationStatus(PersonRegistration.RegistrationStatus.TRAINING_FAILED);
            personRegistrationRepository.save(personRegistration);
        }

        TrainingRecord updatedRecord = trainingRecordRepository.save(record);
        return mapToTrainingRecordDTO(updatedRecord);
    }

    @Transactional
    public TrainingRecordResponseDTO issueCertifications(Long id) {
        TrainingRecord record = trainingRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training record not found with id: " + id));

        // Get all attended sessions for this record
        List<TrainingAttendance> attendances = trainingAttendanceRepository.findByTrainingRecord(record).stream()
                .filter(a -> a.getAttended() != null && a.getAttended() && a.getPassed() != null && a.getPassed())
                .collect(Collectors.toList());

        // Generate certificates for each certification associated with the attended sessions
        for (TrainingAttendance attendance : attendances) {
            TrainingSession session = attendance.getTrainingSession();
            for (Certification certification : session.getCertifications()) {
                // Check if certificate already exists
                if (trainingCertificationRepository.existsByTrainingRecordAndCertification(record, certification)) {
                    continue;
                }

                // Generate certificate
                String certificateNumber = generateCertificateNumber(certification, record);
                LocalDate issuedDate = LocalDate.now();
                LocalDate expiryDate = issuedDate.plusMonths(certification.getValidityPeriodMonths() != null ?
                        certification.getValidityPeriodMonths() : 24);

                TrainingCertification trainingCertification = TrainingCertification.builder()
                        .trainingRecord(record)
                        .certification(certification)
                        .issuedDate(issuedDate)
                        .expiryDate(expiryDate)
                        .certificateNumber(certificateNumber)
                        .build();

                trainingCertificationRepository.save(trainingCertification);
            }
        }

        return mapToTrainingRecordDTO(record);
    }


    @Transactional
    public TrainingSessionResponseDTO updateTrainingSession(Long id, TrainingSessionCreateDTO dto) {
        TrainingSession session = trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));

        User trainer = userRepository.findById(dto.getTrainerId())
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + dto.getTrainerId()));

        // Validate dates
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Create LocalDateTime values for backward compatibility
        LocalDateTime startTime = dto.getStartDate().atTime(dto.getDailyStartTime());
        LocalDateTime endTime = dto.getEndDate().atTime(dto.getDailyEndTime());

        session.setTitle(dto.getTitle());
        session.setDescription(dto.getDescription());
        session.setTrainer(trainer);
        session.setStartDate(dto.getStartDate());
        session.setEndDate(dto.getEndDate());
        session.setDailyStartTime(dto.getDailyStartTime());
        session.setDailyEndTime(dto.getDailyEndTime());
        session.setStartTime(startTime);  // For backward compatibility
        session.setEndTime(endTime);      // For backward compatibility
        session.setLocation(dto.getLocation());
        session.setMaxParticipants(dto.getMaxParticipants());

        // Update certifications
        session.getCertifications().clear();
        if (dto.getCertificationIds() != null && !dto.getCertificationIds().isEmpty()) {
            dto.getCertificationIds().forEach(certId -> {
                Certification cert = certificationRepository.findById(certId)
                        .orElseThrow(() -> new ResourceNotFoundException("Certification not found with id: " + certId));
                session.getCertifications().add(cert);
            });
        }

        TrainingSession updatedSession = trainingSessionRepository.save(session);
        return mapToTrainingSessionDTO(updatedSession);
    }


    public void deleteTrainingSession(Long id) {
        TrainingSession session = trainingSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training session not found with id: " + id));

        // Check if there are attendances for this session
        if (!session.getAttendances().isEmpty()) {
            throw new IllegalStateException("Cannot delete training session with existing attendances");
        }

        trainingSessionRepository.delete(session);
    }

    @Transactional
    public List<TrainingSessionResponseDTO> getUpcomingTrainingSessions() {
        LocalDateTime now = LocalDateTime.now();
        return trainingSessionRepository.findByStartTimeAfter(now).stream()
                .map(this::mapToTrainingSessionDTO)
                .collect(Collectors.toList());
    }



    @Transactional
    public List<TrainingSessionResponseDTO> getTrainingSessionsByTrainer(Long trainerId) {
        User trainer = userRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + trainerId));
        return trainingSessionRepository.findByTrainer(trainer).stream()
                .map(this::mapToTrainingSessionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<TrainingSessionResponseDTO> getTrainingSessionsByStatus(TrainingSession.TrainingStatus status) {
        return trainingSessionRepository.findByStatus(status).stream()
                .map(this::mapToTrainingSessionDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrainingAttendanceResponseDTO> getAttendancesByDate(LocalDate date) {
        // Get all sessions that have any occurrence on this date
        List<TrainingSession> sessions = trainingSessionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date);

        // Get all attendances for these sessions on the specific date
        List<TrainingAttendance> attendances = sessions.stream()
                .flatMap(session ->
                        trainingAttendanceRepository.findByTrainingSessionAndSessionDate(session, date).stream()
                )
                .collect(Collectors.toList());

        return attendances.stream()
                .map(this::mapToTrainingAttendanceDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    protected TrainingSessionResponseDTO mapToTrainingSessionDTO(TrainingSession session) {
        List<TrainingAttendanceResponseDTO> attendances = session.getAttendances().stream()
                .map(this::mapToTrainingAttendanceDTO)
                .collect(Collectors.toList());

        List<CertificationSummaryDTO> certifications = session.getCertifications().stream()
                .map(cert -> CertificationSummaryDTO.builder()
                        .id(cert.getId())
                        .name(cert.getName())
                        .description(cert.getDescription())
                        .issuingAuthority(cert.getIssuingAuthority())
                        .build())
                .collect(Collectors.toList());

        // Calculate daily duration in hours
        long dailyDurationInHours = 0;
        if (session.getDailyStartTime() != null && session.getDailyEndTime() != null) {
            dailyDurationInHours = java.time.Duration.between(
                    session.getDailyStartTime(),
                    session.getDailyEndTime()
            ).toHours();
        }

        return TrainingSessionResponseDTO.builder()
                .id(session.getId())
                .title(session.getTitle())
                .description(session.getDescription())
                .trainerId(session.getTrainer() != null ? session.getTrainer().getId() : null)
                .trainerName(session.getTrainer() != null ?
                        session.getTrainer().getFirstName() + " " + session.getTrainer().getLastName() : null)
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .dailyStartTime(session.getDailyStartTime())
                .dailyEndTime(session.getDailyEndTime())
                .startTime(session.getStartTime())  // For backward compatibility
                .endTime(session.getEndTime())      // For backward compatibility
                .location(session.getLocation())
                .maxParticipants(session.getMaxParticipants())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .certifications(certifications)
                .attendances(attendances)
                .currentParticipants((int) attendances.stream()
                        .map(a -> a.getParticipantName())
                        .distinct()
                        .count())
                .availableSlots(session.getMaxParticipants() != null ?
                        session.getMaxParticipants() - (int) attendances.stream()
                                .map(a -> a.getParticipantName())
                                .distinct()
                                .count() : 0)
                .isFull(session.getMaxParticipants() != null &&
                        attendances.stream()
                                .map(a -> a.getParticipantName())
                                .distinct()
                                .count() >= session.getMaxParticipants())
                .durationInDays(session.getDurationInDays())
                .dailyDurationInHours(dailyDurationInHours)
                .build();
    }


    @Transactional
    protected TrainingAttendanceResponseDTO mapToTrainingAttendanceDTO(TrainingAttendance attendance) {
        System.out.println("DEBUG: Starting to map attendance ID: " + attendance.getId());
        try {
            // Log each property access to find potential NPEs
            String sessionId = "null";
            String sessionTitle = "null";
            String recordId = "null";
            String participantName = "null";
            if (attendance.getTrainingSession() != null) {
                sessionId = String.valueOf(attendance.getTrainingSession().getId());
                sessionTitle = attendance.getTrainingSession().getTitle();
                System.out.println("DEBUG: Session info - ID: " + sessionId + ", Title: " + sessionTitle);
            } else {
                System.out.println("WARNING: TrainingSession is null for attendance ID: " + attendance.getId());
            }
            if (attendance.getTrainingRecord() != null) {
                recordId = String.valueOf(attendance.getTrainingRecord().getId());
                System.out.println("DEBUG: TrainingRecord ID: " + recordId);
            } else {
                System.out.println("WARNING: TrainingRecord is null for attendance ID: " + attendance.getId());
            }
            if (attendance.getPersonRegistration() != null) {
                participantName = attendance.getPersonRegistration().getFirstName() + " " +
                        attendance.getPersonRegistration().getLastName();
                System.out.println("DEBUG: Participant name: " + participantName);
            } else {
                System.out.println("WARNING: PersonRegistration is null for attendance ID: " + attendance.getId());
            }
            String trainerName = "null";
            String sessionLocation = "null";
            if (attendance.getTrainingSession() != null) {
                if (attendance.getTrainingSession().getTrainer() != null) {
                    trainerName = attendance.getTrainingSession().getTrainer().getFirstName() + " " +
                            attendance.getTrainingSession().getTrainer().getLastName();
                    System.out.println("DEBUG: Trainer name: " + trainerName);
                } else {
                    System.out.println("WARNING: Trainer is null for session ID: " + sessionId);
                }
                sessionLocation = attendance.getTrainingSession().getLocation();
                System.out.println("DEBUG: Session location: " + sessionLocation);
            }

            // Calculate session start and end times for this specific date
            LocalDateTime sessionStartTime = null;
            LocalDateTime sessionEndTime = null;
            if (attendance.getSessionDate() != null && attendance.getTrainingSession() != null) {
                if (attendance.getTrainingSession().getDailyStartTime() != null) {
                    sessionStartTime = attendance.getSessionDate().atTime(attendance.getTrainingSession().getDailyStartTime());
                }
                if (attendance.getTrainingSession().getDailyEndTime() != null) {
                    sessionEndTime = attendance.getSessionDate().atTime(attendance.getTrainingSession().getDailyEndTime());
                }
            }

            TrainingAttendanceResponseDTO dto = TrainingAttendanceResponseDTO.builder()
                    .id(attendance.getId())
                    .trainingSessionId(attendance.getTrainingSession() != null ?
                            attendance.getTrainingSession().getId() : null)
                    .trainingSessionTitle(attendance.getTrainingSession() != null ?
                            attendance.getTrainingSession().getTitle() : null)
                    .trainingRecordId(attendance.getTrainingRecord() != null ?
                            attendance.getTrainingRecord().getId() : null)
                    .participantName(attendance.getPersonRegistration() != null ?
                            attendance.getPersonRegistration().getFirstName() + " " +
                                    attendance.getPersonRegistration().getLastName() : null)
                    .registeredAt(attendance.getRegisteredAt())
                    .attended(attendance.getAttended())
                    .checkInTime(attendance.getCheckInTime())
                    .checkOutTime(attendance.getCheckOutTime())
                    .passed(attendance.getPassed())
                    .score(attendance.getScore())
                    .notes(attendance.getNotes())
                    .createdAt(attendance.getCreatedAt())
                    .updatedAt(attendance.getUpdatedAt())
                    .sessionDate(attendance.getSessionDate())
                    .sessionStartTime(sessionStartTime)
                    .sessionEndTime(sessionEndTime)
                    .sessionLocation(attendance.getTrainingSession() != null ?
                            attendance.getTrainingSession().getLocation() : null)
                    .trainerName(attendance.getTrainingSession() != null &&
                            attendance.getTrainingSession().getTrainer() != null ?
                            attendance.getTrainingSession().getTrainer().getFirstName() + " " +
                                    attendance.getTrainingSession().getTrainer().getLastName() : null)
                    .build();
            System.out.println("DEBUG: Successfully mapped attendance ID: " + attendance.getId());
            return dto;
        } catch (Exception e) {
            System.out.println("ERROR: Exception while mapping attendance ID: " + attendance.getId());
            System.out.println("ERROR: Exception message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    @Transactional
    protected TrainingRecordResponseDTO mapToTrainingRecordDTO(TrainingRecord record) {
        List<TrainingAttendanceResponseDTO> attendances = record.getAttendances().stream()
                .map(this::mapToTrainingAttendanceDTO)
                .collect(Collectors.toList());

        List<TrainingCertificationResponseDTO> certifications = record.getCertifications().stream()
                .map(this::mapToTrainingCertificationDTO)
                .collect(Collectors.toList());

        int totalSessionsAttended = (int) attendances.stream()
                .filter(a -> a.getAttended() != null && a.getAttended())
                .count();

        int totalSessionsRequired = attendances.size();
        double attendancePercentage = totalSessionsRequired > 0 ?
                (double) totalSessionsAttended / totalSessionsRequired * 100 : 0;

        return TrainingRecordResponseDTO.builder()
                .id(record.getId())
                .personRegistrationId(record.getPersonRegistration() != null ?
                        record.getPersonRegistration().getId() : null)
                .personName(record.getPersonRegistration() != null ?
                        record.getPersonRegistration().getFirstName() + " " +
                                record.getPersonRegistration().getLastName() : null)
                .enrolledAt(record.getEnrolledAt())
                .completedAt(record.getCompletedAt())
                .overallScore(record.getOverallScore())
                .passed(record.getPassed())
                .trainingStatus(record.getTrainingStatus())
                .trainerNotes(record.getTrainerNotes())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .attendances(attendances)
                .certifications(certifications)
                .totalSessionsAttended(totalSessionsAttended)
                .totalSessionsRequired(totalSessionsRequired)
                .attendancePercentage(attendancePercentage)
                .build();
    }

    @Transactional
    protected TrainingCertificationResponseDTO mapToTrainingCertificationDTO(TrainingCertification certification) {
        LocalDate now = LocalDate.now();
        boolean isExpired = certification.getExpiryDate() != null && certification.getExpiryDate().isBefore(now);
        int daysUntilExpiry = certification.getExpiryDate() != null ?
                (int) java.time.temporal.ChronoUnit.DAYS.between(now, certification.getExpiryDate()) : 0;

        return TrainingCertificationResponseDTO.builder()
                .id(certification.getId())
                .trainingRecordId(certification.getTrainingRecord() != null ?
                        certification.getTrainingRecord().getId() : null)
                .participantName(certification.getTrainingRecord() != null &&
                        certification.getTrainingRecord().getPersonRegistration() != null ?
                        certification.getTrainingRecord().getPersonRegistration().getFirstName() + " " +
                                certification.getTrainingRecord().getPersonRegistration().getLastName() : null)
                .certificationId(certification.getCertification() != null ?
                        certification.getCertification().getId() : null)
                .certificationName(certification.getCertification() != null ?
                        certification.getCertification().getName() : null)
                .certificationDescription(certification.getCertification() != null ?
                        certification.getCertification().getDescription() : null)
                .issuingAuthority(certification.getCertification() != null ?
                        certification.getCertification().getIssuingAuthority() : null)
                .issuedDate(certification.getIssuedDate())
                .expiryDate(certification.getExpiryDate())
                .certificateNumber(certification.getCertificateNumber())
                .isExpired(isExpired)
                .daysUntilExpiry(daysUntilExpiry)
                .createdAt(certification.getCreatedAt())
                .build();
    }
}
