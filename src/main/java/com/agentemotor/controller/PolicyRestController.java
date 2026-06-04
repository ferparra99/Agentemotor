package com.agentemotor.controller;

import com.agentemotor.dto.*;
import com.agentemotor.service.ImportService;
import com.agentemotor.service.PolicyService;
import com.agentemotor.utils.PolicyConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PolicyRestController {

    private final PolicyService policyService;
    private final ImportService importService;

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
    public ResponseEntity<PolicySummaryDTO> createPolicy(@RequestBody PolicyRequestDTO request) {
        return ResponseEntity.ok(policyService.createPolicy(request));
    }

    @PutMapping("/policies/{id}")
    public ResponseEntity<PolicySummaryDTO> updatePolicy(@PathVariable Long id, @RequestBody PolicyRequestDTO request) {
        request.setId(id);
        return ResponseEntity.ok(policyService.updatePolicy(request));
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<ClientDetailDTO> updateClient(@PathVariable Long id, @RequestBody ClientDetailDTO clientData) {
        return ResponseEntity.ok(policyService.updateClient(id, clientData));
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

    @PostMapping(value = "/import/clients", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDTO> importClients(
            @RequestParam("file") MultipartFile file) {
        ImportResultDTO result = importService.importClients(file, PolicyConstants.DEFAULT_ADVISOR_ID);
        return ResponseEntity.ok(result);
    }
}
