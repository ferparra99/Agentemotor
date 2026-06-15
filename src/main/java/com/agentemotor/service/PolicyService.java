package com.agentemotor.service;

import com.agentemotor.dto.*;
import java.util.List;

public interface PolicyService {

    void updatePolicyStatuses(Long advisorId);

    List<PolicySummaryDTO> getPolicies(Long advisorId, String filter);

    PolicyDetailDTO getPolicyDetail(Long policyId);

    PolicySummaryDTO createPolicy(PolicyRequestDTO request);

    PolicySummaryDTO updatePolicy(PolicyRequestDTO request);

    PolicySummaryDTO renewPolicy(Long policyId, RenewRequestDTO request);
}
