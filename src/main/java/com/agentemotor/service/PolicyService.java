package com.agentemotor.service;

import com.agentemotor.dto.*;
import java.util.List;

public interface PolicyService {
    List<PolicySummaryDTO> getPolicies(Long advisorId, String filter);
    DashboardStatsDTO getDashboardStats(Long advisorId);
    PolicyDetailDTO getPolicyDetail(Long policyId);
    ContactAttemptDTO registerContactAttempt(ContactAttemptRequestDTO request);
    PolicySummaryDTO renewPolicy(Long policyId, RenewRequestDTO request);
    PolicySummaryDTO createPolicy(CreatePolicyRequestDTO request);
    ClientDetailDTO getClientDetail(Long clientId);
    List<ClientDetailDTO> getAllClients(Long advisorId);
}
