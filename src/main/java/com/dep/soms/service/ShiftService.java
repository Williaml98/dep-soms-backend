//package com.dep.soms.service;
//
//import com.dep.soms.dto.shift.ShiftDto;
//import com.dep.soms.dto.shift.ShiftRequest;
//import com.dep.soms.exception.ResourceNotFoundException;
//import com.dep.soms.model.Shift;
//import com.dep.soms.model.Site;
//import com.dep.soms.model.ShiftPattern;
//import com.dep.soms.model.Skill;
//import com.dep.soms.repository.ShiftRepository;
//import com.dep.soms.repository.SiteRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//public class ShiftService {
//
//    @Autowired
//    private ShiftRepository shiftRepository;
//
//    @Autowired
//    private SiteRepository siteRepository;
//
////    @Autowired
////    private ShiftPatternRepository shiftPatternRepository;
////
////    @Autowired
////    private SkillRepository skillRepository;
//
//    public List<ShiftDto> getAllShifts() {
//        return shiftRepository.findAll().stream()
//                .map(this::mapShiftToDto)
//                .collect(Collectors.toList());
//    }
//
//    public ShiftDto getShiftById(Long id) {
//        Shift shift = shiftRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
//        return mapShiftToDto(shift);
//    }
//
//    public List<ShiftDto> getShiftsBySiteId(Long siteId) {
//        Site site = siteRepository.findById(siteId)
//                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));
//        return shiftRepository.findBySite(site).stream()
//                .map(this::mapShiftToDto)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public ShiftDto createShift(ShiftRequest request) {
//        Site site = siteRepository.findById(request.getSiteId())
//                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));
//
////        // Get shift pattern if provided
////        ShiftPattern pattern = null;
////        if (request.getPatternId() != null) {
////            pattern = shiftPatternRepository.findById(request.getPatternId())
////                    .orElseThrow(() -> new ResourceNotFoundException("Shift pattern not found with id: " + request.getPatternId()));
////        }
////
////        // Get required skills if provided
////        Set<Skill> requiredSkills = new HashSet<>();
////        if (request.getRequiredSkillIds() != null && !request.getRequiredSkillIds().isEmpty()) {
////            requiredSkills = skillRepository.findAllById(request.getRequiredSkillIds())
////                    .stream().collect(Collectors.toSet());
////        }
//
//        Shift shift = Shift.builder()
//                .site(site)
//                .name(request.getShiftName())
//                .description(request.getDescription())
//                .startTime(request.getStartTime())
//                .endTime(request.getEndTime())
//                .requiredGuards(request.getRequiredGuards())
//                .active(request.isActive())
//                .shiftType(request.getShiftType())
//                .daysOfWeek(request.getDaysOfWeek() != null ? Arrays.toString(request.getDaysOfWeek()) : null)
//                .specificDate(request.getSpecificDate())
//                //.pattern(pattern)
//                .breakDurationMinutes(request.getBreakDurationMinutes())
//                .notes(request.getNotes())
//                .colorCode(request.getColorCode())
//                //.requiredSkills(requiredSkills)
//                // Automatically set latitude and longitude from the selected site
//                .latitude(site.getLatitude())
//                .longitude(site.getLongitude())
//                .minimumRestHours(request.getMinimumRestHours())
//                .build();
//
//        Shift savedShift = shiftRepository.save(shift);
//        return mapShiftToDto(savedShift);
//    }
//
//    @Transactional
//    public ShiftDto updateShift(Long id, ShiftRequest request) {
//        Shift shift = shiftRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
//
//        Site site = siteRepository.findById(request.getSiteId())
//                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));
//
//
//        // Update all fields
//        shift.setSite(site);
//        shift.setName(request.getShiftName());
//        shift.setDescription(request.getDescription());
//        shift.setStartTime(request.getStartTime());
//        shift.setEndTime(request.getEndTime());
//        shift.setRequiredGuards(request.getRequiredGuards());
//        shift.setActive(request.isActive());
//        shift.setShiftType(request.getShiftType());
//        shift.setDaysOfWeek(request.getDaysOfWeek() != null ? Arrays.toString(request.getDaysOfWeek()) : null);
//        shift.setSpecificDate(request.getSpecificDate());
//        //shift.setPattern(pattern);
//        shift.setBreakDurationMinutes(request.getBreakDurationMinutes());
//        shift.setNotes(request.getNotes());
//        shift.setColorCode(request.getColorCode());
//        //shift.setRequiredSkills(requiredSkills);
//        // Update latitude and longitude from the site (in case site changed)
//        shift.setLatitude(site.getLatitude());
//        shift.setLongitude(site.getLongitude());
//        shift.setMinimumRestHours(request.getMinimumRestHours());
//
//        Shift updatedShift = shiftRepository.save(shift);
//        return mapShiftToDto(updatedShift);
//    }
//
//    @Transactional
//    public void deleteShift(Long id) {
//        Shift shift = shiftRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
//        // Soft delete
//        shift.setActive(false);
//        shiftRepository.save(shift);
//    }
//
//    private ShiftDto mapShiftToDto(Shift shift) {
//        return ShiftDto.builder()
//                .id(shift.getId())
//                .siteId(shift.getSite().getId())
//                .siteName(shift.getSite().getName())
//                .name(shift.getName())
//                .description(shift.getDescription())
//                .startTime(shift.getStartTime())
//                .endTime(shift.getEndTime())
//                .requiredGuards(shift.getRequiredGuards())
//                .active(shift.isActive())
//                .shiftType(shift.getShiftType())
//                .daysOfWeek(shift.getDaysOfWeek() != null ?
//                        shift.getDaysOfWeek().replace("[", "").replace("]", "").split(", ") : null)
//                .specificDate(shift.getSpecificDate())
//                .patternId(shift.getPattern() != null ? shift.getPattern().getId() : null)
//                .patternName(shift.getPattern() != null ? shift.getPattern().getName() : null)
//                .breakDurationMinutes(shift.getBreakDurationMinutes())
//                .notes(shift.getNotes())
//                .colorCode(shift.getColorCode())
//                //.requiredSkillIds(shift.getRequiredSkills().stream()
//                //        .map(Skill::getId).collect(Collectors.toSet()))
//               // .requiredSkillNames(shift.getRequiredSkills().stream()
//                 //       .map(Skill::getName).collect(Collectors.toSet()))
//                .latitude(shift.getLatitude())
//                .longitude(shift.getLongitude())
//                .minimumRestHours(shift.getMinimumRestHours())
//                .createdAt(shift.getCreatedAt())
//                .updatedAt(shift.getUpdatedAt())
//                .build();
//    }
//}

package com.dep.soms.service;

import com.dep.soms.dto.shift.ShiftDto;
import com.dep.soms.dto.shift.ShiftRequest;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Shift;
import com.dep.soms.model.Site;
import com.dep.soms.model.ShiftPattern;
import com.dep.soms.model.Skill;
import com.dep.soms.repository.ShiftRepository;
import com.dep.soms.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShiftService {

    private static final Logger logger = LoggerFactory.getLogger(ShiftService.class);

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private SiteRepository siteRepository;
    @Transactional(readOnly = true)
    public List<ShiftDto> getAllShifts() {
        logger.info("Fetching all shifts from database");
        List<Shift> shifts = shiftRepository.findAll();
        logger.info("Found {} shifts in database", shifts.size());

        List<ShiftDto> shiftDtos = shifts.stream()
                .map(this::mapShiftToDto)
                .collect(Collectors.toList());

        logger.info("Mapped {} shifts to DTOs", shiftDtos.size());
        return shiftDtos;
    }
    @Transactional(readOnly = true)
    public ShiftDto getShiftById(Long id) {
        logger.info("Fetching shift with id: {}", id);
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
        logger.info("Found shift: {}", shift.getName());
        return mapShiftToDto(shift);
    }

    @Transactional
    public void deleteShift(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
        // Soft delete
        shift.setActive(false);
        shiftRepository.save(shift);
    }
    @Transactional(readOnly = true)
    public List<ShiftDto> getShiftsBySiteId(Long siteId) {
        logger.info("Fetching shifts for site id: {}", siteId);
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + siteId));

        List<Shift> shifts = shiftRepository.findBySite(site);
        logger.info("Found {} shifts for site: {}", shifts.size(), site.getName());

        return shifts.stream()
                .map(this::mapShiftToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShiftDto createShift(ShiftRequest request) {
        logger.info("Creating new shift: {}", request.getShiftName());
        logger.debug("Shift request: {}", request);

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));

        logger.info("Found site: {}", site.getName());

        Shift shift = Shift.builder()
                .site(site)
                .name(request.getShiftName())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .requiredGuards(request.getRequiredGuards())
                .active(request.isActive())
                .shiftType(request.getShiftType())
                .daysOfWeek(request.getDaysOfWeek() != null ? Arrays.toString(request.getDaysOfWeek()) : null)
                .specificDate(request.getSpecificDate())
                .breakDurationMinutes(request.getBreakDurationMinutes())
                .notes(request.getNotes())
                .colorCode(request.getColorCode())
                .latitude(site.getLatitude())
                .longitude(site.getLongitude())
                .minimumRestHours(request.getMinimumRestHours())
                .build();

        logger.info("Saving shift to database");
        Shift savedShift = shiftRepository.save(shift);
        logger.info("Successfully saved shift with id: {}", savedShift.getId());

        return mapShiftToDto(savedShift);
    }

    @Transactional
    public ShiftDto updateShift(Long id, ShiftRequest request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));


        // Update all fields
        shift.setSite(site);
        shift.setName(request.getShiftName());
        shift.setDescription(request.getDescription());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setRequiredGuards(request.getRequiredGuards());
        shift.setActive(request.isActive());
        shift.setShiftType(request.getShiftType());
        shift.setDaysOfWeek(request.getDaysOfWeek() != null ? Arrays.toString(request.getDaysOfWeek()) : null);
        shift.setSpecificDate(request.getSpecificDate());
        //shift.setPattern(pattern);
        shift.setBreakDurationMinutes(request.getBreakDurationMinutes());
        shift.setNotes(request.getNotes());
        shift.setColorCode(request.getColorCode());
        //shift.setRequiredSkills(requiredSkills);
        // Update latitude and longitude from the site (in case site changed)
        shift.setLatitude(site.getLatitude());
        shift.setLongitude(site.getLongitude());
        shift.setMinimumRestHours(request.getMinimumRestHours());

        Shift updatedShift = shiftRepository.save(shift);
        return mapShiftToDto(updatedShift);
    }

    // ... rest of your methods with similar logging

    private ShiftDto mapShiftToDto(Shift shift) {
        logger.debug("Mapping shift to DTO: {}", shift.getName());
        try {
            return ShiftDto.builder()
                    .id(shift.getId())
                    .siteId(shift.getSite().getId())
                    .siteName(shift.getSite().getName())
                    .name(shift.getName())
                    .description(shift.getDescription())
                    .startTime(shift.getStartTime())
                    .endTime(shift.getEndTime())
                    .requiredGuards(shift.getRequiredGuards())
                    .active(shift.isActive())
                    .shiftType(shift.getShiftType())
                    .daysOfWeek(shift.getDaysOfWeek() != null ?
                            shift.getDaysOfWeek().replace("[", "").replace("]", "").split(", ") : null)
                    .specificDate(shift.getSpecificDate())
                    .patternId(shift.getPattern() != null ? shift.getPattern().getId() : null)
                    .patternName(shift.getPattern() != null ? shift.getPattern().getName() : null)
                    .breakDurationMinutes(shift.getBreakDurationMinutes())
                    .notes(shift.getNotes())
                    .colorCode(shift.getColorCode())
                    .latitude(shift.getLatitude())
                    .longitude(shift.getLongitude())
                    .minimumRestHours(shift.getMinimumRestHours())
                    .createdAt(shift.getCreatedAt())
                    .updatedAt(shift.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            logger.error("Error mapping shift to DTO: {}", shift.getId(), e);
            throw e;
        }
    }
}
