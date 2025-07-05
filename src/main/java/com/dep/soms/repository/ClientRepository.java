package com.dep.soms.repository;

import com.dep.soms.model.Client;
import com.dep.soms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUser(User user);
    List<Client> findByActive(boolean active);
    Optional<Client> findByContractNumber(String contractNumber);


    @Query("SELECT c FROM Client c WHERE c.contractEndDate <= :date")
    List<Client> findByContractEndDateBefore(LocalDate date);

    Optional<Client> findByUserId(Long userId);

    @Query("SELECT s.id FROM Client c JOIN c.sites s WHERE c.user.id = :userId")
    List<Long> findSiteIdsByClientUserId(@Param("userId") Long userId);

}
