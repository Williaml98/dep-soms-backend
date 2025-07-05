package com.dep.soms.service;

import com.dep.soms.dto.client.ClientDto;
import com.dep.soms.dto.client.ClientRegistrationRequest;
import com.dep.soms.dto.client.ClientUpdateRequest;
import com.dep.soms.exception.ResourceNotFoundException;
import com.dep.soms.model.Client;
import com.dep.soms.model.Role;
import com.dep.soms.model.User;
import com.dep.soms.repository.ClientRepository;
import com.dep.soms.repository.RoleRepository;
import com.dep.soms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    public List<ClientDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ClientDto getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        return mapToDto(client);
    }

    public ClientDto getClientByUserId(Long userId) {
        Client client = clientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found for user with id: " + userId));
        return mapToDto(client);
    }


//    @Transactional
//    public ClientDto registerClient(ClientRegistrationRequest request) {
//        // First create the user
//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new IllegalArgumentException("Username is already taken");
//        }
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new IllegalArgumentException("Email is already in use");
//        }
//
//        User user = User.builder()
//                .username(request.getUsername())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .firstName(request.getContactPerson())
//                .lastName("")
//                .phoneNumber(request.getPhoneNumber())
//                .active(true)
//                .build();
//
//        // Assign CLIENT role
//        Set<Role> roles = new HashSet<>();
//        Role clientRole = roleRepository.findByName(Role.ERole.ROLE_CLIENT)
//                .orElseThrow(() -> new RuntimeException("Error: Client Role is not found."));
//        roles.add(clientRole);
//        user.setRoles(roles);
//
//        User savedUser = userRepository.save(user);
//
//        // Now create the client
//        Client client = Client.builder()
//                .user(savedUser)
//                .companyName(request.getCompanyName())
//                .contactPerson(request.getContactPerson())
//                .address(request.getAddress())
//                //.city(request.getCity())
//                //.state(request.getState())
//                //.zipCode(request.getZipCode())
//                //.country(request.getCountry())
//                //.industry(request.getIndustry())
//                .active(true)
//                //.notes(request.getNotes())
//                .build();
//
//        Client savedClient = clientRepository.save(client);
//        return mapToDto(savedClient);
//    }

//    @Transactional
//    public ClientDto registerClient(ClientRegistrationRequest request) {
//        // First create the user
//        if (userRepository.existsByUsername(request.getUsername())) {
//            throw new IllegalArgumentException("Username is already taken");
//        }
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new IllegalArgumentException("Email is already in use");
//        }
//
//        // Store the original password for notification
//        String originalPassword = request.getPassword();
//
//        LocalDateTime now = LocalDateTime.now();
//
//        User user = User.builder()
//                .username(request.getUsername())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(originalPassword))
//                .firstName(request.getContactPerson())
//                .lastName("")
//                .phoneNumber(request.getPhoneNumber())
//                .active(true)
//                .createdAt(now)
//                .build();
//
//        // Assign CLIENT role
//        Set<Role> roles = new HashSet<>();
//        Role clientRole = roleRepository.findByName(Role.ERole.ROLE_CLIENT)
//                .orElseThrow(() -> new RuntimeException("Error: Client Role is not found."));
//        roles.add(clientRole);
//        user.setRoles(roles);
//
//        User savedUser = userRepository.save(user);
//
//        // Now create the client
//        Client client = Client.builder()
//                .user(savedUser)
//                .companyName(request.getCompanyName())
//                .contactPerson(request.getContactPerson())
//                .address(request.getAddress())
//                .active(true)
//                .createdAt(now)
//                .build();
//
//        Client savedClient = clientRepository.save(client);
//
//        // Send notifications
//        emailService.sendAccountCreationEmail(
//                request.getEmail(),
//                request.getUsername(),
//                originalPassword,
//                "Client"
//        );
//
//        smsService.sendAccountCreationSms(
//                request.getPhoneNumber(),
//                request.getUsername(),
//                "Client"
//        );
//
//        return mapToDto(savedClient);
//    }

    @Transactional
    public ClientDto updateClient(Long id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        User user = client.getUser();

        // Update user information
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getContactPerson() != null) {
            user.setFirstName(request.getContactPerson());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);

        // Update client information
        if (request.getCompanyName() != null) {
            client.setCompanyName(request.getCompanyName());
        }

        if (request.getContactPerson() != null) {
            client.setContactPerson(request.getContactPerson());
        }

        if (request.getAddress() != null) {
            client.setAddress(request.getAddress());
        }

//        if (request.getCity() != null) {
//            client.setCity(request.getCity());
//        }
//
//        if (request.getState() != null) {
//            client.setState(request.getState());
//        }
//
//        if (request.getZipCode() != null) {
//            client.setZipCode(request.getZipCode());
//        }
//
//        if (request.getCountry() != null) {
//            client.setCountry(request.getCountry());
//        }
//
//        if (request.getIndustry() != null) {
//            client.setIndustry(request.getIndustry());
//        }
//
//        client.setActive(request.isActive());
//
//        if (request.getNotes() != null) {
//            client.setNotes(request.getNotes());
//        }

        Client updatedClient = clientRepository.save(client);
        return mapToDto(updatedClient);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        User user = client.getUser();
        user.setActive(false);
        userRepository.save(user);

        client.setActive(false);
        clientRepository.save(client);
    }

@Transactional
public ClientDto createClient(ClientRegistrationRequest request) {
    // Check if username exists
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new IllegalArgumentException("Username is already taken");
    }

    // Check if email exists
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Email is already in use");
    }

    // Set default password if not provided
    String password = request.getPassword();
    if (password == null || password.isEmpty()) {
        password = "123456"; // Default password
    }

    // Create user account with proper name handling
    User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(password))
            // Use the firstname and lastname from request, not the client name
            .firstName(request.getFirstname() != null ? request.getFirstname() : "")
            .lastName(request.getLastname() != null ? request.getLastname() : "")
            .phoneNumber(request.getPhoneNumber())
            .active(true)
            .build();

    // Set role
    Set<Role> roles = new HashSet<>();
    Role clientRole = roleRepository.findByName(Role.ERole.ROLE_CLIENT)
            .orElseThrow(() -> new RuntimeException("Error: Role CLIENT is not found."));
    roles.add(clientRole);
    user.setRoles(roles);

    User savedUser = userRepository.save(user);

    // Determine client name - fix the logic here
    String clientName = null;

    // First try to use the provided name
    if (request.getName() != null && !request.getName().trim().isEmpty()) {
        clientName = request.getName().trim();
    }
    // If name is not provided, use company name as fallback
    else if (request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty()) {
        clientName = request.getCompanyName().trim();
    }
    // If both are null/empty, throw an exception
    else {
        throw new IllegalArgumentException("Either client name or company name must be provided");
    }

    System.out.println("Final resolved client name: '" + clientName + "'");
    System.out.println("Request name: '" + request.getName() + "'");
    System.out.println("Request company name: '" + request.getCompanyName() + "'");

    // Validate that clientName is not null before proceeding
    if (clientName == null || clientName.trim().isEmpty()) {
        throw new IllegalArgumentException("Client name cannot be null or empty after resolution");
    }

    // Create client with ALL fields mapped properly
    Client client = Client.builder()
            .user(savedUser)
            .name(clientName)
            .companyName(request.getCompanyName())
            .address(request.getAddress())
            .city(request.getCity())
            .country(request.getCountry())
            .industry(request.getIndustry())
            .notes(request.getNotes())
            .contactPerson(request.getContactPerson())
            // Fix: Use contactEmail from request, not just email
            .contactEmail(request.getContactEmail() != null ? request.getContactEmail() : request.getEmail())
            // Fix: Use contactPhone from request, not just phoneNumber
            .contactPhone(request.getContactPhone() != null ? request.getContactPhone() : request.getPhoneNumber())
            // Fix: Add the missing contract fields
            .contractStartDate(request.getContractStartDate())
            .contractEndDate(request.getContractEndDate())
            .contractNumber(request.getContractNumber())
            .active(true)
            .build();

    Client savedClient = clientRepository.save(client);

    // Store the original password for notification
    String originalPassword = request.getPassword();

    // Send notifications
    emailService.sendAccountCreationEmail(
            request.getEmail(),
            request.getUsername(),
            originalPassword,
            "Client"
    );

    return mapToDto(savedClient);
}
    private ClientDto mapToDto(Client client) {
        User user = client.getUser();

        return ClientDto.builder()
                .id(client.getId())
                .userId(user != null ? user.getId() : null)
                .name(client.getName())
                .companyName(client.getCompanyName())
                .address(client.getAddress())
                .contactPerson(client.getContactPerson())
                .contactEmail(client.getContactEmail())
                .contactPhone(client.getContactPhone())
                .contractStartDate(client.getContractStartDate())
                .contractEndDate(client.getContractEndDate())
                .contractNumber(client.getContractNumber())
                .active(client.isActive())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                //.sites(siteMapper.mapSites(client.getSites())) // Optional: if you want to map sites too
                .build();

//        return ClientDto.builder()
//                .id(client.getId())
//                .userId(user.getId())
//                .username(user.getUsername())
//                .contactEmail(user.getEmail())
//                .firstname(user.getFirstName())
//                .lastname(user.getLastName())
//                .contactPhone(user.getPhoneNumber())
//                .companyName(client.getCompanyName())
//                .address(client.getAddress())
//                .contactPerson(client.getContactPerson())
//                .active(user.isActive())
//                .build();
    }


}

