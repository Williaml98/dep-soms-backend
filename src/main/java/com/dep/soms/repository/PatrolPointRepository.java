//package com.dep.soms.repository;
//
//import com.dep.soms.model.PatrolPoint;
//import com.dep.soms.model.Site;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface PatrolPointRepository extends JpaRepository<PatrolPoint, Long> {
//
//    // Find all active patrol points for a specific site
//   // List<PatrolPoint> findBySiteAndIsActive(Site site, boolean isActive);
//
//    List<PatrolPoint> findBySiteAndActive(Site site, boolean active);
//
//    // Find all patrol points for a site (both active and inactive)
//    List<PatrolPoint> findBySite(Site site);
//
//    // Find patrol points by site ID
//    List<PatrolPoint> findBySiteId(Long siteId);
//
//    // Find active patrol points by site ID
//    List<PatrolPoint> findBySiteIdAndIsActive(Long siteId, boolean isActive);
//
//    // Find patrol points by sequence number for a specific site
//    List<PatrolPoint> findBySiteAndSequenceNumber(Site site, Integer sequenceNumber);
//
//    // Find patrol points containing name (case insensitive)
//    List<PatrolPoint> findByNameContainingIgnoreCase(String name);
//
//    // Count active patrol points for a site
//    long countBySiteAndIsActive(Site site, boolean isActive);
//}

package com.dep.soms.repository;

import com.dep.soms.model.PatrolPoint;
import com.dep.soms.model.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatrolPointRepository extends JpaRepository<PatrolPoint, Long> {

    // Find all active patrol points for a specific site
    List<PatrolPoint> findBySiteAndActive(Site site, boolean active);

    // Find all patrol points for a site (both active and inactive)
    List<PatrolPoint> findBySite(Site site);

    // Find patrol points by site ID
    List<PatrolPoint> findBySiteId(Long siteId);

    // Find active patrol points by site ID
    List<PatrolPoint> findBySiteIdAndActive(Long siteId, boolean active);

    // Find patrol points by sequence number for a specific site
    List<PatrolPoint> findBySiteAndSequenceNumber(Site site, Integer sequenceNumber);

    // Find patrol points containing name (case insensitive)
    List<PatrolPoint> findByNameContainingIgnoreCase(String name);

    // Count active patrol points for a site
    long countBySiteAndActive(Site site, boolean active);
}