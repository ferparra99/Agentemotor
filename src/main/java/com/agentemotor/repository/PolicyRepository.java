package com.agentemotor.repository;

import com.agentemotor.model.Policy;
import com.agentemotor.model.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    List<Policy> findByAdvisorId(Long advisorId);

    List<Policy> findByAdvisorIdAndStatus(Long advisorId, PolicyStatus status);

    List<Policy> findByAdvisorIdAndStatusIn(Long advisorId, List<PolicyStatus> statuses);

    List<Policy> findByClientId(Long clientId);

    List<Policy> findByClientIdAndStatus(Long clientId, PolicyStatus status);
}
