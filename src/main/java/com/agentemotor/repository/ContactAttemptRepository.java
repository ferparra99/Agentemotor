package com.agentemotor.repository;

import com.agentemotor.model.ContactAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactAttemptRepository extends JpaRepository<ContactAttempt, Long> {
    List<ContactAttempt> findByPolicyIdOrderByDateDesc(Long policyId);
    Optional<ContactAttempt> findTopByPolicyIdOrderByDateDesc(Long policyId);
    int countByPolicyId(Long policyId);
}
