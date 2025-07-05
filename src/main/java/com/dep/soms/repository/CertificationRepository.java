package com.dep.soms.repository;

import com.dep.soms.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Optional<Certification> findByName(String name);
    Optional<Certification> findByCode(String code);
}
