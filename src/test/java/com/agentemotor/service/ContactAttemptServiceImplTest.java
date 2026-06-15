package com.agentemotor.service;

import com.agentemotor.dto.ContactAttemptDTO;
import com.agentemotor.dto.ContactAttemptRequestDTO;
import com.agentemotor.model.*;
import com.agentemotor.repository.ContactAttemptRepository;
import com.agentemotor.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactAttemptServiceImplTest {

    @Mock
    private ContactAttemptRepository contactAttemptRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private ContactAttemptServiceImpl contactAttemptService;

    private Policy testPolicy;

    @BeforeEach
    void setUp() {
        Advisor advisor = Advisor.builder()
                .id(1L).name("Asesor").email("a@test.co").phone("3000000000")
                .build();

        Client client = Client.builder()
                .id(10L).name("Cliente").phone("3100000001").advisor(advisor)
                .build();

        testPolicy = Policy.builder()
                .id(100L)
                .policyNumber("AUTO-001")
                .type(PolicyType.AUTO)
                .insurer("Sura")
                .startDate(LocalDate.now().minusMonths(6))
                .expirationDate(LocalDate.now().plusMonths(6))
                .status(PolicyStatus.ACTIVO)
                .renewalCount(0)
                .client(client)
                .advisor(advisor)
                .build();
    }

    @Test
    @DisplayName("registerContactAttempt saves and returns DTO")
    void registerContactAttempt_success() {
        ContactAttemptRequestDTO request = new ContactAttemptRequestDTO();
        request.setPolicyId(100L);
        request.setType("CALL");
        request.setResult("CONTACTED");
        request.setNotes("Cliente contactado exitosamente.");

        ContactAttempt savedAttempt = ContactAttempt.builder()
                .id(1L)
                .policy(testPolicy)
                .date(LocalDateTime.now())
                .type(ContactAttemptType.CALL)
                .result(ContactAttemptResult.CONTACTED)
                .notes("Cliente contactado exitosamente.")
                .build();

        when(policyRepository.findById(100L)).thenReturn(Optional.of(testPolicy));
        when(contactAttemptRepository.save(any(ContactAttempt.class))).thenReturn(savedAttempt);

        ContactAttemptDTO result = contactAttemptService.registerContactAttempt(request);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("CALL");
        assertThat(result.getResult()).isEqualTo("CONTACTED");
        assertThat(result.getNotes()).isEqualTo("Cliente contactado exitosamente.");
    }

    @Test
    @DisplayName("registerContactAttempt throws when policy not found")
    void registerContactAttempt_policyNotFound() {
        ContactAttemptRequestDTO request = new ContactAttemptRequestDTO();
        request.setPolicyId(999L);

        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactAttemptService.registerContactAttempt(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Policy not found");
    }

    @Test
    @DisplayName("getAttemptsByPolicy returns list of DTOs")
    void getAttemptsByPolicy() {
        ContactAttempt attempt = ContactAttempt.builder()
                .id(1L)
                .policy(testPolicy)
                .date(LocalDateTime.now())
                .type(ContactAttemptType.WHATSAPP)
                .result(ContactAttemptResult.INTERESTED)
                .notes("Interesado en renovar")
                .build();

        when(contactAttemptRepository.findByPolicyIdOrderByDateDesc(100L))
                .thenReturn(List.of(attempt));

        List<ContactAttemptDTO> result = contactAttemptService.getAttemptsByPolicy(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("WHATSAPP");
        assertThat(result.get(0).getResult()).isEqualTo("INTERESTED");
    }

    @Test
    @DisplayName("findLastAttemptByPolicy returns last attempt")
    void findLastAttemptByPolicy() {
        ContactAttempt attempt = ContactAttempt.builder()
                .id(1L)
                .policy(testPolicy)
                .date(LocalDateTime.now())
                .type(ContactAttemptType.CALL)
                .result(ContactAttemptResult.NO_ANSWER)
                .build();

        when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(100L))
                .thenReturn(Optional.of(attempt));

        Optional<ContactAttempt> result = contactAttemptService.findLastAttemptByPolicy(100L);

        assertThat(result).isPresent();
        assertThat(result.get().getResult()).isEqualTo(ContactAttemptResult.NO_ANSWER);
    }

    @Test
    @DisplayName("countAttemptsByPolicy returns correct count")
    void countAttemptsByPolicy() {
        when(contactAttemptRepository.countByPolicyId(100L)).thenReturn(5);

        int count = contactAttemptService.countAttemptsByPolicy(100L);

        assertThat(count).isEqualTo(5);
    }
}
