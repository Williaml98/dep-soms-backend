package com.dep.soms.repository;

import com.dep.soms.model.Client;
import com.dep.soms.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    List<Site> findByClient(Client client);
    List<Site> findByClientAndActive(Client client, boolean active);
    Optional<Site> findBySiteCode(String siteCode);
    // In SiteRepository.java
    List<Site> findByClientId(Long clientId);
}