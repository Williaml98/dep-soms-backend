//package com.dep.soms.controller;
//
//import com.dep.soms.dto.auth.JwtResponse;
//import com.dep.soms.dto.auth.LoginRequest;
//import com.dep.soms.dto.auth.MessageResponse;
//import com.dep.soms.dto.auth.SignupRequest;
//import com.dep.soms.model.Role;
//import com.dep.soms.model.User;
//import com.dep.soms.repository.RoleRepository;
//import com.dep.soms.repository.UserRepository;
//import com.dep.soms.security.jwt.JwtUtils;
//import com.dep.soms.security.services.UserDetailsImpl;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//        @CrossOrigin(origins = "*", maxAge = 3600)
//        @RestController
//        @RequestMapping("/api/auth")
//        public class AuthController {
//        @Autowired
//        AuthenticationManager authenticationManager;
//
//        @Autowired
//        UserRepository userRepository;
//
//        @Autowired
//        RoleRepository roleRepository;
//
//        @Autowired
//        PasswordEncoder encoder;
//
//        @Autowired
//        JwtUtils jwtUtils;
//
//        @PostMapping("/signin")
//        public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//    Authentication authentication = authenticationManager.authenticate(
//            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//
//    SecurityContextHolder.getContext().setAuthentication(authentication);
//    String jwt = jwtUtils.generateJwtToken(authentication);
//
//    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//    List<String> roles = userDetails.getAuthorities().stream()
//            .map(item -> item.getAuthority())
//            .collect(Collectors.toList());
//
//    // Update last login time
//    User user = userRepository.findById(userDetails.getId()).orElseThrow();
//    user.setLastLogin(LocalDateTime.now());
//    userRepository.save(user);
//
//    return ResponseEntity.ok(new JwtResponse(jwt,
//            userDetails.getId(),
//            userDetails.getUsername(),
//            userDetails.getEmail(),
//            roles));
//}
//
//@PostMapping("/signup")
//public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
//    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
//        return ResponseEntity
//                .badRequest()
//                .body(new MessageResponse("Error: Username is already taken!"));
//    }
//
//    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//        return ResponseEntity
//                .badRequest()
//                .body(new MessageResponse("Error: Email is already in use!"));
//    }
//
//    // Create new user's account
//    User user = User.builder()
//            .username(signUpRequest.getUsername())
//            .email(signUpRequest.getEmail())
//            .password(encoder.encode(signUpRequest.getPassword()))
//            .firstName(signUpRequest.getFirstName())
//            .lastName(signUpRequest.getLastName())
//            .phoneNumber(signUpRequest.getPhoneNumber())
//            .preferredLanguage(signUpRequest.getPreferredLanguage())
//            .active(true)
//            .build();
//
//    Set<String> strRoles = signUpRequest.getRoles();
//    Set<Role> roles = new HashSet<>();
//
//    if (strRoles == null) {
//        Role userRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
//                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//        roles.add(userRole);
//    } else {
//        strRoles.forEach(role -> {
//            switch (role) {
//                case "admin":
//                    Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(adminRole);
//                    break;
//                case "manager":
//                    Role modRole = roleRepository.findByName(Role.ERole.ROLE_MANAGER)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(modRole);
//                    break;
//                case "client":
//                    Role clientRole = roleRepository.findByName(Role.ERole.ROLE_CLIENT)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(clientRole);
//                    break;
//                default:
//                    Role userRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(userRole);
//            }
//        });
//    }
//
//    user.setRoles(roles);
//    userRepository.save(user);
//
//    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
//}
//
//}

package com.dep.soms.controller;

import com.dep.soms.dto.auth.JwtResponse;
import com.dep.soms.dto.auth.LoginRequest;
import com.dep.soms.dto.auth.SignupRequest;
import com.dep.soms.model.Client;
import com.dep.soms.model.Guard;
import com.dep.soms.model.Role.ERole;
import com.dep.soms.model.Role;
import com.dep.soms.model.User;
import com.dep.soms.repository.ClientRepository;
import com.dep.soms.repository.GuardRepository;
import com.dep.soms.repository.RoleRepository;
import com.dep.soms.repository.UserRepository;
import com.dep.soms.security.jwt.JwtUtils;
import com.dep.soms.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    GuardRepository guardRepository;
    @Autowired
    ClientRepository clientRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    // LOGIN

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Authentication attempt for user: " + loginRequest.getUsername());
            // Check if user exists
            if (!userRepository.existsByUsername(loginRequest.getUsername())) {
                System.out.println("User does not exist: " + loginRequest.getUsername());
                return ResponseEntity.status(401).body(Map.of(
                        "message", "Invalid username or password"
                ));
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Add debug logging
            System.out.println("Generated JWT token: " + jwt);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            System.out.println("Authentication successful for user: " + userDetails.getUsername());
            System.out.println("Roles: " + roles);

            // Create response map with token
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("token", jwt);
            responseMap.put("id", userDetails.getId());
            responseMap.put("username", userDetails.getUsername());
            responseMap.put("email", userDetails.getEmail());
            responseMap.put("roles", roles);
            responseMap.put("type", "Bearer");
            responseMap.put("success", true);

            // Add debug logging for response
            System.out.println("Response token: " + responseMap.get("token"));
            System.out.println("Response object: " + responseMap);

            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Invalid username or password",
                    "error", e.getMessage()
            ));
        }
    }


//    @PostMapping("/signin")
//    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//        try {
//            System.out.println("Authentication attempt for user: " + loginRequest.getUsername());
//
//            // Check if user exists
//            if (!userRepository.existsByUsername(loginRequest.getUsername())) {
//                System.out.println("User does not exist: " + loginRequest.getUsername());
//                return ResponseEntity.status(401).body(Map.of(
//                        "message", "Invalid username or password"
//                ));
//            }
//
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            loginRequest.getUsername(), loginRequest.getPassword())
//            );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            String jwt = jwtUtils.generateJwtToken(authentication);
//
//            System.out.println("Generated JWT token: " + jwt);
//
//            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//            List<String> roles = userDetails.getAuthorities()
//                    .stream()
//                    .map(item -> item.getAuthority())
//                    .collect(Collectors.toList());
//
//            System.out.println("Authentication successful for user: " + userDetails.getUsername());
//            System.out.println("Roles: " + roles);
//
//            return ResponseEntity.ok(new JwtResponse(jwt,
//                    userDetails.getId(),
//                    userDetails.getUsername(),
//                    userDetails.getEmail(),
//                    roles));
//        } catch (Exception e) {
//            System.out.println("Authentication failed: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(401).body(Map.of(
//                    "message", "Invalid username or password",
//                    "error", e.getMessage()
//            ));
//        }
//    }



    // SIGNUP
//    @PostMapping("/signup")
//    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
//
//        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
//            return ResponseEntity
//                    .badRequest()
//                    .body("Error: Username is already taken!");
//        }
//
//        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//            return ResponseEntity
//                    .badRequest()
//                    .body("Error: Email is already in use!");
//        }
//
//        // Create new user
//        User user = new User();
//        user.setUsername(signUpRequest.getUsername());
//        user.setEmail(signUpRequest.getEmail());
//        user.setPassword(encoder.encode(signUpRequest.getPassword()));
//        user.setFirstName(signUpRequest.getFirstName());
//        user.setLastName(signUpRequest.getLastName());
//        user.setPhoneNumber(signUpRequest.getPhoneNumber());
//        user.setPreferredLanguage(signUpRequest.getPreferredLanguage());
//        user.setActive(true);
//
//        Set<String> strRoles = signUpRequest.getRoles();
//        Set<Role> roles = new HashSet<>();
//
//        if (strRoles == null || strRoles.isEmpty()) {
//            Role userRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
//                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//            roles.add(userRole);
//        } else {
//            strRoles.forEach(role -> {
//                switch (role.toLowerCase()) {
//                    case "admin":
//                        Role adminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                        roles.add(adminRole);
//                        break;
//                    case "mod":
//                        Role modRole = roleRepository.findByName(Role.ERole.ROLE_GUARD)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                        roles.add(modRole);
//                        break;
//                    default:
//                        Role userRole = roleRepository.findByName(Role.ERole.ROLE_CLIENT)
//                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                        roles.add(userRole);
//                }
//            });
//        }
//
//        user.setRoles(roles);
//        userRepository.save(user);
//
//        return ResponseEntity.ok("User registered successfully!");
//    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .phoneNumber(signUpRequest.getPhoneNumber())
                .preferredLanguage(signUpRequest.getPreferredLanguage())
                .active(true)
                .build();

        // Assign roles
        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(roleRepository.findByName(ERole.valueOf("ROLE_USER")).orElseThrow());
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin" -> roles.add(roleRepository.findByName(ERole.valueOf("ROLE_ADMIN")).orElseThrow());
                    case "guard" -> roles.add(roleRepository.findByName(ERole.valueOf("ROLE_GUARD")).orElseThrow());
                    case "client" -> roles.add(roleRepository.findByName(ERole.valueOf("ROLE_CLIENT")).orElseThrow());
                    default -> roles.add(roleRepository.findByName(ERole.valueOf("ROLE_USER")).orElseThrow());
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        // Try to link to Guard
        if (strRoles.contains("guard") && signUpRequest.getBadgeNumber() != null) {
            Optional<Guard> guardOpt = guardRepository.findByBadgeNumber(signUpRequest.getBadgeNumber());
            guardOpt.ifPresent(guard -> {
                if (guard.getUser() == null) {
                    guard.setUser(user);
                    guardRepository.save(guard);
                }
            });
        }

        // Try to link to Client
        if (strRoles.contains("client") && signUpRequest.getContractNumber() != null) {
            Optional<Client> clientOpt = clientRepository.findByContractNumber(signUpRequest.getContractNumber());
            clientOpt.ifPresent(client -> {
                if (client.getUser() == null) {
                    client.setUser(user);
                    clientRepository.save(client);
                }
            });
        }

        return ResponseEntity.ok("User registered successfully!");
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok("Auth endpoint is working!");
    }

    @GetMapping("/whoami")
    public ResponseEntity<?> whoAmI(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                return ResponseEntity.ok(Map.of(
                        "username", username,
                        "isValid", jwtUtils.validateJwtToken(jwt)
                ));
            }
            return ResponseEntity.status(401).body("No valid token provided");
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Token validation failed",
                    "message", e.getMessage()
            ));
        }
    }

}
