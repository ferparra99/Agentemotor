package com.agentemotor.service;

import com.agentemotor.dto.ContactAttemptDTO;
import com.agentemotor.dto.ContactAttemptRequestDTO;
import com.agentemotor.model.*;
import com.agentemotor.repository.ContactAttemptRepository;
import com.agentemotor.repository.PolicyRepository;
import com.agentemotor.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactAttemptServiceImpl implements ContactAttemptService {

    private final ContactAttemptRepository contactAttemptRepository;
    private final PolicyRepository policyRepository;

    @Override
    @Transactional
    public ContactAttemptDTO registerContactAttempt(ContactAttemptRequestDTO request) {
        Policy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.POLICY_NOT_FOUND + request.getPolicyId()));

        ContactAttempt attempt = ContactAttempt.builder()
                .policy(policy)
                .date(LocalDateTime.now())
                .type(ContactAttemptType.valueOf(request.getType()))
                .result(ContactAttemptResult.valueOf(request.getResult()))
                .notes(request.getNotes())
                .build();

        attempt = contactAttemptRepository.save(attempt);

        return ContactAttemptDTO.builder()
                .id(attempt.getId())
                .date(attempt.getDate())
                .type(attempt.getType().name())
                .result(attempt.getResult().name())
                .notes(attempt.getNotes())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactAttemptDTO> getAttemptsByPolicy(Long policyId) {
        return contactAttemptRepository.findByPolicyIdOrderByDateDesc(policyId).stream()
                .map(a -> ContactAttemptDTO.builder()
                        .id(a.getId())
                        .date(a.getDate())
                        .type(a.getType().name())
                        .result(a.getResult().name())
                        .notes(a.getNotes())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactAttempt> findLastAttemptByPolicy(Long policyId) {
        return contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(policyId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countAttemptsByPolicy(Long policyId) {
        return contactAttemptRepository.countByPolicyId(policyId);
    }
}
