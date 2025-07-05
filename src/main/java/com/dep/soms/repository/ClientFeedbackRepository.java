package com.dep.soms.repository;

import com.dep.soms.model.ClientFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientFeedbackRepository extends JpaRepository<ClientFeedback, Long> {
    List<ClientFeedback> findByClientId(Long clientId);

    List<ClientFeedback> findBySiteId(Long siteId);

    List<ClientFeedback> findByGuardId(Long guardId);

    @Query("SELECT AVG(cf.rating) FROM ClientFeedback cf WHERE cf.client.id = :clientId")
    Double getAverageRatingByClientId(Long clientId);

    @Query("SELECT AVG(cf.rating) FROM ClientFeedback cf WHERE cf.site.id = :siteId")
    Double getAverageRatingBySiteId(Long siteId);

    @Query("SELECT AVG(cf.guardRating) FROM ClientFeedback cf WHERE cf.guard.id = :guardId")
    Double getAverageGuardRatingByGuardId(Long guardId);
}
