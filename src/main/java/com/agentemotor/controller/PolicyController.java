package com.agentemotor.controller;

import com.agentemotor.controller.api.PolicyApi;
import com.agentemotor.dto.*;
import com.agentemotor.service.ContactAttemptService;
import com.agentemotor.service.PolicyService;
import com.agentemotor.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController implements PolicyApi {

    private final PolicyService policyService;
    private final ContactAttemptService contactAttemptService;

    @Override
    @GetMapping("/list")
    public ResponseEntity<List<PolicySummaryDTO>> listByAdvisorAndFilter(
            @RequestParam Long advisorId,
            @RequestParam(defaultValue = AppConstants.FILTER_ALL) String filter) {
        return ResponseEntity.ok(policyService.getPolicies(advisorId, filter));
    }

    @Override
    @GetMapping("/detail/{id}")
    public ResponseEntity<PolicyDetailDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyDetail(id));
    }

    @Override
    @PostMapping("/create")
    public ResponseEntity<PolicySummaryDTO> create(@RequestBody PolicyRequestDTO request) {
        return ResponseEntity.ok(policyService.createPolicy(request));
    }

    @Override
    @PutMapping("/update/{id}")
    public ResponseEntity<PolicySummaryDTO> update(@PathVariable Long id, @RequestBody PolicyRequestDTO request) {
        request.setId(id);
        return ResponseEntity.ok(policyService.updatePolicy(request));
    }

    @Override
    @PutMapping("/renew/{id}")
    public ResponseEntity<PolicySummaryDTO> renew(@PathVariable Long id, @RequestBody RenewRequestDTO request) {
        return ResponseEntity.ok(policyService.renewPolicy(id, request));
    }

    @Override
    @PostMapping("/register-attempt/{id}")
    public ResponseEntity<ContactAttemptDTO> registerAttempt(
            @PathVariable Long id,
            @RequestBody ContactAttemptRequestDTO request) {
        request.setPolicyId(id);
        return ResponseEntity.ok(contactAttemptService.registerContactAttempt(request));
    }
}
