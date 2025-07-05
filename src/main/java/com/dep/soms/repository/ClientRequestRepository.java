package com.dep.soms.repository;

import com.dep.soms.model.ClientRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRequestRepository extends JpaRepository<ClientRequest, Long> {
    List<ClientRequest> findByClientId(Long clientId);

    @Query("SELECT cr FROM ClientRequest cr WHERE cr.requestedBy.id = :userId")
    List<ClientRequest> findByRequestedBy(@Param("userId") Long userId);

    @Query("SELECT cr FROM ClientRequest cr WHERE cr.assignedTo.id = :userId")
    List<ClientRequest> findByAssignedTo(@Param("userId") Long userId);
}