package com.dep.soms.controller;

import com.dep.soms.dto.auth.MessageResponse;
import com.dep.soms.dto.user.*;
import com.dep.soms.model.Role;
import com.dep.soms.security.services.UserDetailsImpl;
import com.dep.soms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PutMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserDto> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody ProfileUpdateRequest request) {
        UserDto updatedUser = userService.updateProfile(id, request);
        return ResponseEntity.ok(updatedUser);
    }


    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeRequest request) {

        System.out.println("=== PASSWORD CHANGE REQUEST ===");
        System.out.println("User ID: " + id);
        System.out.println("Current Password provided: " + (request.getCurrentPassword() != null && !request.getCurrentPassword().isEmpty()));
        System.out.println("New Password provided: " + (request.getNewPassword() != null && !request.getNewPassword().isEmpty()));
        System.out.println("Confirm Password provided: " + (request.getConfirmPassword() != null && !request.getConfirmPassword().isEmpty()));

        try {
            userService.changePassword(id, request);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (IllegalArgumentException e) {
            System.err.println("Password change error: " + e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error during password change: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Failed to change password"));
        }
    }


    @PostMapping("/{id}/profile-picture")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserDto> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        UserDto updatedUser = userService.uploadProfilePicture(id, file);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserProfileDto profile = userService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserProfileDto updatedProfile = userService.updateUserProfile(userDetails.getId(), updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserProfileDto> getUserById(@PathVariable Long id) {
        UserProfileDto user = userService.getUserProfile(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        UserProfileDto updatedUser = userService.updateUserProfile(id, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
//        userService.deleteUser(id);
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        userService.setUserPassword(request);
        return ResponseEntity.ok().body(new MessageResponse("Password set successfully"));
    }

//
@GetMapping("/guards")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public ResponseEntity<List<UserDto>> getAllGuards() {
    System.out.println("=== GUARDS ENDPOINT HIT ===");
    System.out.println("Timestamp: " + java.time.LocalDateTime.now());

    // Add debug call
    userService.debugRolesAndUsers();

    try {
        List<UserDto> guards = userService.getAllGuards();
        System.out.println("Found " + guards.size() + " guards");
        return ResponseEntity.ok(guards);
    } catch (Exception e) {
        System.err.println("Error in getAllGuards: " + e.getMessage());
        e.printStackTrace();
        throw e;
    }
}

//    @GetMapping("/supervisors")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR')")
//    public ResponseEntity<List<UserDto>> getAllSupervisors() {
//        List<UserDto> supervisors = userService.getAllSupervisors();
//        return ResponseEntity.ok(supervisors);
//    }

    @GetMapping("/supervisors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<UserDto>> getAllSupervisors() {
        System.out.println("=== SUPERVISORS ENDPOINT HIT ===");
        System.out.println("Timestamp: " + java.time.LocalDateTime.now());

        try {
            List<UserDto> supervisors = userService.getAllSupervisors();
            System.out.println("Found " + supervisors.size() + " supervisors");
            return ResponseEntity.ok(supervisors);
        } catch (Exception e) {
            System.err.println("Error in getAllSupervisors: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    // You can also add a more generic endpoint to get users by role
    @GetMapping("/by-role/{roleName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String roleName) {
        try {
            Role.ERole role = Role.ERole.valueOf("ROLE_" + roleName.toUpperCase());
            List<UserDto> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }




}
