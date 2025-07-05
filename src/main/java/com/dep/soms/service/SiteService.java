package com.dep.soms.service;

import com.dep.soms.dto.site.SiteDto;
import com.dep.soms.dto.site.SiteRequest;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Client;
import com.dep.soms.model.Site;
import com.dep.soms.repository.ClientRepository;
import com.dep.soms.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteService {
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private ClientRepository clientRepository;
    @Transactional(readOnly = true)
    public List<SiteDto> getAllSites() {
        return siteRepository.findAll().stream()
                .map(this::mapSiteToDto)
                .collect(Collectors.toList());
    }

    public SiteDto getSiteById(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));

        return mapSiteToDto(site);
    }
    @Transactional(readOnly = true)
    public List<SiteDto> getSitesByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        return siteRepository.findByClient(client).stream()
                .map(this::mapSiteToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SiteDto createSite(SiteRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        // Check if site code already exists
        if (siteRepository.findBySiteCode(request.getSiteCode()).isPresent()) {
            throw new IllegalArgumentException("Site code already exists");
        }

        Site site = Site.builder()
                .client(client)
                .name(request.getName())
                .siteCode(request.getSiteCode())
                .address(request.getAddress())
                .city(request.getCity())
                //.state(request.getState())
                //.zipCode(request.getZipCode())
                .country(request.getCountry())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
               //.contactEmail(request.getContactEmail())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .active(request.isActive())
                //.notes(request.getNotes())
                .build();

        Site savedSite = siteRepository.save(site);
        return mapSiteToDto(savedSite);
    }

    @Transactional
    public SiteDto updateSite(Long id, SiteRequest request) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));

        // Check if client exists
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        // Check if site code already exists (if changed)
        if (!request.getSiteCode().equals(site.getSiteCode()) &&
                siteRepository.findBySiteCode(request.getSiteCode()).isPresent()) {
            throw new IllegalArgumentException("Site code already exists");
        }

        site.setClient(client);
        site.setName(request.getName());
        site.setSiteCode(request.getSiteCode());
        site.setAddress(request.getAddress());
        site.setCity(request.getCity());
//        site.setState(request.getState());
//        site.setZipCode(request.getZipCode());
        site.setCountry(request.getCountry());
        site.setContactPerson(request.getContactPerson());
        site.setContactPhone(request.getContactPhone());
        //site.setContactEmail(request.getContactEmail());
        site.setLatitude(request.getLatitude());
        site.setLongitude(request.getLongitude());
        site.setActive(request.isActive());
        //site.setNotes(request.getNotes());

        Site updatedSite = siteRepository.save(site);
        return mapSiteToDto(updatedSite);
    }

    @Transactional
    public void deleteSite(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + id));

        // Soft delete
        site.setActive(false);
        siteRepository.save(site);
    }

    private SiteDto mapSiteToDto(Site site) {
        return SiteDto.builder()
                .id(site.getId())
                .clientId(site.getClient().getId())
                .clientName(site.getClient().getCompanyName())
                .name(site.getName())
                .siteCode(site.getSiteCode())
                .address(site.getAddress())
                .city(site.getCity())
//                .state(site.getState())
//                .zipCode(site.getZipCode())
                 .country(site.getCountry())
                .contactPerson(site.getContactPerson())
                .contactPhone(site.getContactPhone())
               // .contactEmail(site.getContactEmail())
                .latitude(site.getLatitude())
                .longitude(site.getLongitude())
                .active(site.isActive())
                //.notes(site.getNotes())
                .build();
    }
}
