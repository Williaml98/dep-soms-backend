package com.dep.soms.dto.person;

import lombok.Data;

import java.util.List;

@Data
public class TrainingAssignmentRequestDTO {
    private Long sessionId;
    private List<Long> recruitIds;
}