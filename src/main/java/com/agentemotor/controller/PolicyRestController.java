package com.agentemotor.controller;

import com.agentemotor.dto.*;
import com.agentemotor.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PolicyRestController {

    private final PolicyService policyService;

    @GetMapping("/policies")
    public ResponseEntity<List<PolicySummaryDTO>> listPolicies(
            @RequestParam Long advisorId,
            @RequestParam(defaultValue = "all") String filter) {
        return ResponseEntity.ok(policyService.getPolicies(advisorId, filter));
    }

    @GetMapping("/policies/{id}")
    public ResponseEntity<PolicyDetailDTO> getPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyDetail(id));
    }

    @PostMapping("/policies")
    public ResponseEntity<PolicySummaryDTO> createPolicy(@RequestBody CreatePolicyRequestDTO request) {
        return ResponseEntity.ok(policyService.createPolicy(request));
    }

    @PutMapping("/policies/{id}/renew")
    public ResponseEntity<PolicySummaryDTO> renewPolicy(
            @PathVariable Long id,
            @RequestBody RenewRequestDTO request) {
        return ResponseEntity.ok(policyService.renewPolicy(id, request));
    }

    @PostMapping("/contact-attempts")
    public ResponseEntity<ContactAttemptDTO> registerAttempt(
            @RequestBody ContactAttemptRequestDTO request) {
        return ResponseEntity.ok(policyService.registerContactAttempt(request));
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientDetailDTO>> listClients(@RequestParam Long advisorId) {
        return ResponseEntity.ok(policyService.getAllClients(advisorId));
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<ClientDetailDTO> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getClientDetail(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(@RequestParam Long advisorId) {
        return ResponseEntity.ok(policyService.getDashboardStats(advisorId));
    }
}
