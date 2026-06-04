package com.agentemotor.service;

import com.agentemotor.dto.*;
import com.agentemotor.model.Client;
import java.util.List;

public interface PolicyService {
    List<PolicySummaryDTO> getPolicies(Long advisorId, String filter);
    DashboardStatsDTO getDashboardStats(Long advisorId);
    PolicyDetailDTO getPolicyDetail(Long policyId);
    ContactAttemptDTO registerContactAttempt(ContactAttemptRequestDTO request);
    PolicySummaryDTO renewPolicy(Long policyId, RenewRequestDTO request);
    PolicySummaryDTO createPolicy(PolicyRequestDTO request);
    PolicySummaryDTO updatePolicy(PolicyRequestDTO request);
    ClientDetailDTO updateClient(Long clientId, ClientDetailDTO clientData);
    ClientDetailDTO getClientDetail(Long clientId);
    List<ClientDetailDTO> getAllClients(Long advisorId);
    Client findOrCreateClient(String name, String phone, String email, String notes, Long advisorId);
}
