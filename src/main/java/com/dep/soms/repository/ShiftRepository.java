package com.dep.soms.repository;

import com.dep.soms.model.Shift;
import com.dep.soms.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findBySite(Site site);
    List<Shift> findBySiteAndActive(Site site, boolean active);
}
