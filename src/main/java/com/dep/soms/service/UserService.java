package com.dep.soms.service;

import com.dep.soms.dto.user.*;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Role;
import com.dep.soms.model.User;
import com.dep.soms.repository.ClientRepository;
import com.dep.soms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    ClientRepository clientRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return mapToDto(user);
    }

    @Transactional
    public UserDto updateProfile(Long id, ProfileUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public void changePassword(Long id, PasswordChangeRequest request) {
        System.out.println("=== CHANGE PASSWORD SERVICE ===");
        System.out.println("User ID: " + id);
        System.out.println("Request: " + request);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        System.out.println("Found user: " + user.getUsername());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            System.err.println("Current password verification failed");
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            System.err.println("Password confirmation mismatch");
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        System.out.println("Password validation passed, updating...");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        System.out.println("Password updated successfully");
    }

    // Add this method to your existing UserService

    /**
     * Get all site IDs associated with a client user
     */
    public List<Long> getSiteIdsForClient(Long userId) {
        // This implementation depends on your data model
        // Here's a simplified example assuming you have a Client entity with sites
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // This is just an example - implement based on your actual data model
        return clientRepository.findSiteIdsByClientUserId(userId);
    }


    @Transactional
    public UserDto uploadProfilePicture(Long id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);

            // Copy file to upload directory
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update user profile picture
            user.setProfilePicture(filename);
            User updatedUser = userRepository.save(user);

            return mapToDto(updatedUser);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }


    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .active(user.isActive())
                .profilePicture(user.getProfilePicture())
                .build();
    }

    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapUserToProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty()) {
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }

        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }

        if (updateRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updateRequest.getPhoneNumber());
        }

        if (updateRequest.getPreferredLanguage() != null) {
            user.setPreferredLanguage(updateRequest.getPreferredLanguage());
        }

        // Update password if provided
        if (updateRequest.getCurrentPassword() != null && updateRequest.getNewPassword() != null) {
            if (passwordEncoder.matches(updateRequest.getCurrentPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
            } else {
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }

        User updatedUser = userRepository.save(user);
        return mapUserToProfileDto(updatedUser);
    }

//    public List<UserProfileDto> getAllUsers() {
//        return userRepository.findAll().stream()
//                .map(this::mapUserToProfileDto)
//                .collect(Collectors.toList());
//    }

    // Add this method to your UserService class
    @Transactional
    public List<UserDto> getUsersByRole(Role.ERole roleName) {
        return userRepository.findActiveUsersByRoleName(roleName).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Add this specific method for supervisors
    @Transactional
    public List<UserDto> getAllSupervisors() {
        return userRepository.findActiveUsersByRoleName(Role.ERole.ROLE_SUPERVISOR).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setActive(false);
        userRepository.save(user);
    }

    private UserProfileDto mapUserToProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .active(user.isActive())
                .lastLogin(user.getLastLogin())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .preferredLanguage(user.getPreferredLanguage())
                .build();
    }

// Replace the existing setUserPassword method with this one
// Remove the @Override annotation since you're not implementing an interface method

    public void setUserPassword(SetPasswordRequest request) {
        // Validate that passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new com.dep.soms.exception.BadRequestException("Passwords do not match");
        }

        // Find user by username and email
        User user = userRepository.findByUsernameAndEmail(request.getUsername(), request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " +
                        request.getUsername() + " and email: " + request.getEmail()));

        // Check if password is already set
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            throw new com.dep.soms.exception.BadRequestException("Password is already set for this user");
        }

        // Encode and set the password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save the updated user
        userRepository.save(user);
    }

//    public List<UserDto> getAllGuards() {
//        return userRepository.findByRoles_Name(Role.ERole.ROLE_GUARD).stream()
//                .map(this::mapToDto)
//                .collect(Collectors.toList());
//    }

    public List<UserDto> getAllGuards() {
        // Use the new method that handles enums properly
        return userRepository.findActiveUsersByRoleName(Role.ERole.ROLE_GUARD).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void debugRolesAndUsers() {
        System.out.println("=== DEBUG: ALL USERS AND ROLES ===");

        // Get all users
        List<User> allUsers = userRepository.findAll();
        System.out.println("Total users in database: " + allUsers.size());

        allUsers.forEach(user -> {
            System.out.println("User ID: " + user.getId() +
                    ", Username: " + user.getUsername() +
                    ", Active: " + user.isActive());

            if (user.getRoles() != null) {
                user.getRoles().forEach(role -> {
                    System.out.println("  - Role: " + role.getName() + " (ID: " + role.getId() + ")");
                });
            } else {
                System.out.println("  - No roles assigned");
            }
        });

        // Check what we're searching for
        System.out.println("Searching for role: " + Role.ERole.ROLE_GUARD);
    }



}
