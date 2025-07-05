package com.dep.soms.controller;

import com.dep.soms.dto.site.SiteDto;
import com.dep.soms.dto.site.SiteRequest;
import com.dep.soms.service.SiteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sites")
public class SiteController {
    @Autowired
    private SiteService siteService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<List<SiteDto>> getAllSites() {
        List<SiteDto> sites = siteService.getAllSites();
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT') or hasRole('GUARD')")
    public ResponseEntity<SiteDto> getSiteById(@PathVariable Long id) {
        SiteDto site = siteService.getSiteById(id);
        return ResponseEntity.ok(site);
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<List<SiteDto>> getSitesByClientId(@PathVariable Long clientId) {
        List<SiteDto> sites = siteService.getSitesByClientId(clientId);
        return ResponseEntity.ok(sites);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SiteDto> createSite(@Valid @RequestBody SiteRequest siteRequest) {
        SiteDto createdSite = siteService.createSite(siteRequest);
        return ResponseEntity.ok(createdSite);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SiteDto> updateSite(
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest siteRequest) {
        SiteDto updatedSite = siteService.updateSite(id, siteRequest);
        return ResponseEntity.ok(updatedSite);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSite(@PathVariable Long id) {
        siteService.deleteSite(id);
        return ResponseEntity.ok().build();
    }



}

