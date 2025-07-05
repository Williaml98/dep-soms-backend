package com.dep.soms.repository;

import com.dep.soms.model.Certification;
import com.dep.soms.model.TrainingCertification;
import com.dep.soms.model.TrainingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingCertificationRepository extends JpaRepository<TrainingCertification, Long> {
    List<TrainingCertification> findByTrainingRecord(TrainingRecord record);
    boolean existsByTrainingRecordAndCertification(TrainingRecord record, Certification certification);
    List<TrainingCertification> findByCertification(Certification certification);
    // Add this method to TrainingCertificationRepository
    Optional<TrainingCertification> findByCertificateNumber(String certificateNumber);

}
