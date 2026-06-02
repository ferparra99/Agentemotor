package com.agentemotor.service;

import com.agentemotor.dto.*;
import com.agentemotor.model.*;
import com.agentemotor.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class PolicyServiceTest {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private AdvisorRepository advisorRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PolicyRepository policyRepository;

    private Long advisorId;

    @BeforeEach
    void setUp() {
        Advisor advisor = advisorRepository.save(
                Advisor.builder().name("María").email("maria@test.co").phone("3000000000").build());
        advisorId = advisor.getId();

        Client client1 = clientRepository.save(
                Client.builder().name("Cliente A").phone("3100000001").advisor(advisor).build());
        Client client2 = clientRepository.save(
                Client.builder().name("Cliente B").phone("3100000002").advisor(advisor).build());

        LocalDate today = LocalDate.now();

        Policy expired5days = Policy.builder()
                .policyNumber("AUTO-TEST-001")
                .type(PolicyType.AUTO)
                .insurer("Test Insurer")
                .startDate(today.minusMonths(12))
                .expirationDate(today.minusDays(5))
                .status(PolicyStatus.ACTIVA)
                .renewalCount(0)
                .client(client1)
                .advisor(advisor)
                .build();
        policyRepository.save(expired5days);

        Policy expired35days = Policy.builder()
                .policyNumber("AUTO-TEST-002")
                .type(PolicyType.AUTO)
                .insurer("Test Insurer")
                .startDate(today.minusMonths(13))
                .expirationDate(today.minusDays(35))
                .status(PolicyStatus.ACTIVA)
                .renewalCount(0)
                .client(client2)
                .advisor(advisor)
                .build();
        policyRepository.save(expired35days);

        Policy activePolicy = Policy.builder()
                .policyNumber("AUTO-TEST-003")
                .type(PolicyType.AUTO)
                .insurer("Test Insurer")
                .startDate(today.minusMonths(6))
                .expirationDate(today.plusDays(60))
                .status(PolicyStatus.ACTIVA)
                .renewalCount(0)
                .client(client1)
                .advisor(advisor)
                .build();
        policyRepository.save(activePolicy);
    }

    @Test
    @DisplayName("Policy expired 5 days should be high priority, 35 days should be lost")
    void test30DayWindowClassification() {
        List<PolicySummaryDTO> allPolicies = policyService.getPolicies(advisorId, "all");

        PolicySummaryDTO expired5days = allPolicies.stream()
                .filter(p -> p.getPolicyNumber().equals("AUTO-TEST-001"))
                .findFirst().orElseThrow();

        PolicySummaryDTO expired35days = allPolicies.stream()
                .filter(p -> p.getPolicyNumber().equals("AUTO-TEST-002"))
                .findFirst().orElseThrow();

        assertThat(expired5days.getDaysOverdue()).isEqualTo(5);
        assertThat(expired5days.getPriority()).isEqualTo("alta");

        assertThat(expired35days.getDaysOverdue()).isEqualTo(35);
        assertThat(expired35days.getPriority()).isEqualTo("perdido");

        List<PolicySummaryDTO> withinWindow = policyService.getPolicies(advisorId, "expired_lt_30");
        assertThat(withinWindow).anyMatch(p -> p.getPolicyNumber().equals("AUTO-TEST-001"));
        assertThat(withinWindow).noneMatch(p -> p.getPolicyNumber().equals("AUTO-TEST-002"));

        List<PolicySummaryDTO> beyondWindow = policyService.getPolicies(advisorId, "expired_gt_30");
        assertThat(beyondWindow).anyMatch(p -> p.getPolicyNumber().equals("AUTO-TEST-002"));
        assertThat(beyondWindow).noneMatch(p -> p.getPolicyNumber().equals("AUTO-TEST-001"));
    }

    @Test
    @DisplayName("Renewing a policy marks it RENEWED and creates a new ACTIVE policy")
    void testPolicyRenewal() {
        List<PolicySummaryDTO> allPolicies = policyService.getPolicies(advisorId, "all");

        PolicySummaryDTO policyToRenew = allPolicies.stream()
                .filter(p -> p.getPolicyNumber().equals("AUTO-TEST-001"))
                .findFirst().orElseThrow();

        RenewRequestDTO renewRequest = new RenewRequestDTO();
        renewRequest.setNewExpirationDate(LocalDate.now().plusYears(1));

        PolicySummaryDTO renewed = policyService.renewPolicy(policyToRenew.getId(), renewRequest);

        assertThat(renewed).isNotNull();
        assertThat(renewed.getStatus()).isEqualTo("ACTIVA");
        assertThat(renewed.getPolicyNumber()).contains("-R1");

        Policy originalPolicy = policyRepository.findById(policyToRenew.getId()).orElseThrow();
        assertThat(originalPolicy.getStatus()).isEqualTo(PolicyStatus.RENOVADA);

        List<Policy> clientPolicies = policyRepository.findByClientId(originalPolicy.getClient().getId());
        long activeCount = clientPolicies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVA)
                .count();

        long renewedCount = clientPolicies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.RENOVADA)
                .count();

        assertThat(activeCount).isEqualTo(2);
        assertThat(renewedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Registering a contact attempt persists it and increments the count")
    void testContactAttemptRegistration() {
        List<PolicySummaryDTO> allPolicies = policyService.getPolicies(advisorId, "all");

        PolicySummaryDTO target = allPolicies.stream()
                .filter(p -> p.getPolicyNumber().equals("AUTO-TEST-003"))
                .findFirst().orElseThrow();

        assertThat(target.getContactAttempts()).isEqualTo(0);

        ContactAttemptRequestDTO attemptRequest = new ContactAttemptRequestDTO();
        attemptRequest.setPolicyId(target.getId());
        attemptRequest.setType("CALL");
        attemptRequest.setResult("CONTACTED");
        attemptRequest.setNotes("Cliente interesado en renovar.");

        ContactAttemptDTO result = policyService.registerContactAttempt(attemptRequest);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("CALL");
        assertThat(result.getResult()).isEqualTo("CONTACTED");
        assertThat(result.getNotes()).isEqualTo("Cliente interesado en renovar.");

        List<PolicySummaryDTO> updatedPolicies = policyService.getPolicies(advisorId, "all");
        PolicySummaryDTO updated = updatedPolicies.stream()
                .filter(p -> p.getPolicyNumber().equals("AUTO-TEST-003"))
                .findFirst().orElseThrow();

        assertThat(updated.getContactAttempts()).isEqualTo(1);
    }
}
