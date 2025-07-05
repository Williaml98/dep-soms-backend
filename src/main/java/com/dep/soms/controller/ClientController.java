package com.dep.soms.controller;

import com.dep.soms.dto.client.ClientDto;
import com.dep.soms.dto.client.ClientRegistrationRequest;
import com.dep.soms.dto.client.ClientUpdateRequest;
import com.dep.soms.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<ClientDto> getClientByUserId(@PathVariable Long userId) {
        ClientDto client = clientService.getClientByUserId(userId);
        return ResponseEntity.ok(client);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CLIENT')")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        ClientDto client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ClientDto> registerClient(@Valid @RequestBody ClientRegistrationRequest request) {
        ClientDto createdClient = clientService.createClient(request);
        return ResponseEntity.ok(createdClient);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ClientDto> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateRequest request) {
        ClientDto updatedClient = clientService.updateClient(id, request);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok().build();
    }

//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//
//    public ResponseEntity<List<ClientDto>> getAllClients() {
//        List<ClientDto> clients = clientService.getAllClients();
//        return ResponseEntity.ok(clients);
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @userSecurity.isCurrentClient(#id)")
//    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
//        ClientDto client = clientService.getClientById(id);
//        return ResponseEntity.ok(client);
//    }

//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientRegistrationRequest request) {
//        ClientDto createdClient = clientService.createClient(request);
//        return ResponseEntity.ok(createdClient);
//    }

//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentClient(#id)")
//    public ResponseEntity<ClientDto> updateClient(
//            @PathVariable Long id,
//            @Valid @RequestBody ClientUpdateRequest request) {
//        ClientDto updatedClient = clientService.updateClient(id, request);
//        return ResponseEntity.ok(updatedClient);
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> deleteClient(@PathVariable Long id) {
//        clientService.deleteClient(id);
//        return ResponseEntity.ok().build();
//    }

}

