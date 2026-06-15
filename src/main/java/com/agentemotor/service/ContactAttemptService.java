package com.agentemotor.service;

import com.agentemotor.dto.ContactAttemptDTO;
import com.agentemotor.dto.ContactAttemptRequestDTO;
import com.agentemotor.model.ContactAttempt;
import java.util.List;
import java.util.Optional;

public interface ContactAttemptService {

    ContactAttemptDTO registerContactAttempt(ContactAttemptRequestDTO request);

    List<ContactAttemptDTO> getAttemptsByPolicy(Long policyId);

    Optional<ContactAttempt> findLastAttemptByPolicy(Long policyId);

    int countAttemptsByPolicy(Long policyId);
}
