package com.dep.soms.dto.person;

import com.dep.soms.model.TrainingRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecordUpdateDTO {
    private Integer overallScore;
    private Boolean passed;
    private TrainingRecord.TrainingStatus trainingStatus;
    private String trainerNotes;
}
