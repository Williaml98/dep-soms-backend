package com.dep.soms.controller;

import com.dep.soms.dto.client.*;
import com.dep.soms.service.ClientRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client-requests")
@RequiredArgsConstructor
public class ClientRequestController {

    private final ClientRequestService clientRequestService;

    @GetMapping
    public ResponseEntity<List<ClientRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(clientRequestService.getAllRequests());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ClientRequestDTO>> getRequestsByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientRequestService.getRequestsByClient(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientRequestDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(clientRequestService.getRequestById(id));
    }

    @PostMapping
    public ResponseEntity<ClientRequestDTO> createRequest(
            @RequestBody CreateClientRequestDTO requestDTO) {
        return ResponseEntity.ok(clientRequestService.createRequest(requestDTO, requestDTO.getClientId()));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ClientRequestDTO> updateRequest(
            @PathVariable Long id,
            @RequestBody UpdateClientRequestDTO requestDTO) {
        return ResponseEntity.ok(clientRequestService.updateRequest(id, requestDTO));
    }



//    @PostMapping("/{requestId}/comments")
//    public ResponseEntity<ClientRequestCommentDTO> addComment(
//            @PathVariable Long requestId,
//            @RequestBody AddCommentDTO commentDTO,
//            @AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = Long.parseLong(userDetails.getUsername());
//        return ResponseEntity.ok(clientRequestService.addComment(commentDTO, userId));
//    }

    @PostMapping("/{requestId}/comments")
    public ResponseEntity<ClientRequestCommentDTO> addComment(
            @PathVariable Long requestId,
            @RequestBody AddCommentDTO commentDTO) {
        // Set the requestId from the path parameter
        commentDTO.setClientRequestId(requestId);
        return ResponseEntity.ok(clientRequestService.addComment(commentDTO, commentDTO.getUserId()));
    }




    @GetMapping("/{requestId}/comments")
    public ResponseEntity<List<ClientRequestCommentDTO>> getCommentsByRequestId(@PathVariable Long requestId) {
        return ResponseEntity.ok(clientRequestService.getCommentsByRequestId(requestId));
    }

}