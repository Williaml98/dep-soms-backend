package com.dep.soms.repository;

import com.dep.soms.model.PersonRegistration;
import com.dep.soms.model.TrainingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {
    Optional<TrainingRecord> findByPersonRegistration(PersonRegistration personRegistration);
    List<TrainingRecord> findByTrainingStatus(TrainingRecord.TrainingStatus status);
}
