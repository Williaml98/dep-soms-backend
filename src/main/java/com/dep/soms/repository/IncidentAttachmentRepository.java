package com.dep.soms.repository;

import com.dep.soms.model.IncidentAttachment;
import com.dep.soms.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentAttachmentRepository extends JpaRepository<IncidentAttachment, Long> {

    List<IncidentAttachment> findByIncident(Incident incident);
    List<IncidentAttachment> findByIncidentId(Long incidentId);
    void deleteByIncidentId(Long incidentId);
}
