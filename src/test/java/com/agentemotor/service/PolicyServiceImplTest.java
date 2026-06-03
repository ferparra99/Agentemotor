package com.agentemotor.service;

import com.agentemotor.dto.*;
import com.agentemotor.model.*;
import com.agentemotor.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ContactAttemptRepository contactAttemptRepository;

    @Mock
    private AdvisorRepository advisorRepository;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private Advisor defaultAdvisor;
    private Client testClient;
    private Policy testPolicy;

    @BeforeEach
    void setUp() {
        defaultAdvisor = Advisor.builder()
                .id(1L)
                .name("María Asesora")
                .email("maria@test.co")
                .phone("+57 300 123 4567")
                .build();

        testClient = Client.builder()
                .id(10L)
                .name("Juan Pérez")
                .phone("+57 310 111 2233")
                .email("juan@email.com")
                .notes("Cliente nuevo")
                .advisor(defaultAdvisor)
                .build();

        testPolicy = Policy.builder()
                .id(100L)
                .policyNumber("AUTO-010-2026")
                .type(PolicyType.AUTO)
                .insurer("Seguros Sura")
                .startDate(LocalDate.now().minusMonths(1))
                .expirationDate(LocalDate.now().plusMonths(11))
                .status(PolicyStatus.ACTIVO)
                .renewalCount(0)
                .client(testClient)
                .advisor(defaultAdvisor)
                .build();

        ReflectionTestUtils.setField(policyService, "self", policyService);
    }

    // ──────────────────────────────────────────────
    //  createPolicy()
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("createPolicy()")
    class CreatePolicyTests {

        @Captor
        private ArgumentCaptor<Client> clientCaptor;

        @Captor
        private ArgumentCaptor<Policy> policyCaptor;

        @Test
        @DisplayName("creates client and policy successfully with all fields")
        void createPolicy_success() {
            PolicyRequestDTO request = PolicyRequestDTO.builder()
                    .clientName("Nuevo Cliente")
                    .clientPhone("+57 300 999 8877")
                    .clientEmail("nuevo@email.com")
                    .clientNotes("Nota de prueba")
                    .policyNumber("AUTO-999-2026")
                    .type("AUTO")
                    .insurer("Mapfre")
                    .startDate(LocalDate.of(2026, 1, 1))
                    .expirationDate(LocalDate.of(2027, 1, 1))
                    .build();

            Client savedClient = Client.builder()
                    .id(99L)
                    .name("Nuevo Cliente")
                    .phone("+57 300 999 8877")
                    .email("nuevo@email.com")
                    .notes("Nota de prueba")
                    .advisor(defaultAdvisor)
                    .build();

            Policy savedPolicy = Policy.builder()
                    .id(200L)
                    .policyNumber("AUTO-999-2026")
                    .type(PolicyType.AUTO)
                    .insurer("Mapfre")
                    .startDate(LocalDate.of(2026, 1, 1))
                    .expirationDate(LocalDate.of(2027, 1, 1))
                    .status(PolicyStatus.ACTIVO)
                    .renewalCount(0)
                    .client(savedClient)
                    .advisor(defaultAdvisor)
                    .build();

            when(advisorRepository.findById(1L)).thenReturn(Optional.of(defaultAdvisor));
            when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
            when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);
            when(contactAttemptRepository.countByPolicyId(200L)).thenReturn(0);

            PolicySummaryDTO result = policyService.createPolicy(request);

            assertThat(result).isNotNull();
            assertThat(result.getPolicyNumber()).isEqualTo("AUTO-999-2026");
            assertThat(result.getClientName()).isEqualTo("Nuevo Cliente");
            assertThat(result.getClientPhone()).isEqualTo("+57 300 999 8877");
            assertThat(result.getType()).isEqualTo("AUTO");
            assertThat(result.getInsurer()).isEqualTo("Mapfre");
            assertThat(result.getStatus()).isEqualTo("ACTIVO");

            verify(clientRepository).save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getName()).isEqualTo("Nuevo Cliente");
            assertThat(clientCaptor.getValue().getPhone()).isEqualTo("+57 300 999 8877");
            assertThat(clientCaptor.getValue().getEmail()).isEqualTo("nuevo@email.com");
            assertThat(clientCaptor.getValue().getNotes()).isEqualTo("Nota de prueba");

            verify(policyRepository).save(policyCaptor.capture());
            assertThat(policyCaptor.getValue().getPolicyNumber()).isEqualTo("AUTO-999-2026");
            assertThat(policyCaptor.getValue().getRenewalCount()).isZero();
        }

        @Test
        @DisplayName("creates client with empty phone when not provided")
        void createPolicy_withoutPhone() {
            PolicyRequestDTO request = PolicyRequestDTO.builder()
                    .clientName("Cliente Sin Teléfono")
                    .policyNumber("HOGAR-001-2026")
                    .type("HOGAR")
                    .insurer("Allianz")
                    .startDate(LocalDate.of(2026, 3, 1))
                    .expirationDate(LocalDate.of(2027, 3, 1))
                    .build();

            Client savedClient = Client.builder()
                    .id(98L)
                    .name("Cliente Sin Teléfono")
                    .phone("")
                    .advisor(defaultAdvisor)
                    .build();

            Policy savedPolicy = Policy.builder()
                    .id(201L)
                    .policyNumber("HOGAR-001-2026")
                    .type(PolicyType.HOGAR)
                    .insurer("Allianz")
                    .startDate(LocalDate.of(2026, 3, 1))
                    .expirationDate(LocalDate.of(2027, 3, 1))
                    .status(PolicyStatus.ACTIVO)
                    .renewalCount(0)
                    .client(savedClient)
                    .advisor(defaultAdvisor)
                    .build();

            when(advisorRepository.findById(1L)).thenReturn(Optional.of(defaultAdvisor));
            when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
            when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);
            when(contactAttemptRepository.countByPolicyId(201L)).thenReturn(0);

            PolicySummaryDTO result = policyService.createPolicy(request);

            assertThat(result.getClientName()).isEqualTo("Cliente Sin Teléfono");
            assertThat(result.getClientPhone()).isEmpty();

            verify(clientRepository).save(clientCaptor.capture());
            assertThat(clientCaptor.getValue().getPhone()).isEmpty();
        }

        @Test
        @DisplayName("throws exception when advisor is not found")
        void createPolicy_advisorNotFound() {
            PolicyRequestDTO request = PolicyRequestDTO.builder()
                    .clientName("Test")
                    .policyNumber("P-001")
                    .type("AUTO")
                    .insurer("Test")
                    .startDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusYears(1))
                    .build();

            when(advisorRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> policyService.createPolicy(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Advisor not found");

            verify(clientRepository, never()).save(any());
            verify(policyRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws exception for invalid policy type")
        void createPolicy_invalidType() {
            PolicyRequestDTO request = PolicyRequestDTO.builder()
                    .clientName("Test")
                    .policyNumber("P-001")
                    .type("INVALID_TYPE")
                    .insurer("Test")
                    .startDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusYears(1))
                    .build();

            when(advisorRepository.findById(1L)).thenReturn(Optional.of(defaultAdvisor));

            assertThatThrownBy(() -> policyService.createPolicy(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────
    //  updatePolicy()
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("updatePolicy()")
    class UpdatePolicyTests {

        @Test
        @DisplayName("updates all fields successfully")
        void updatePolicy_allFields() {
            PolicyRequestDTO request = PolicyRequestDTO.builder()
                    .id(100L)
                    .policyNumber("AUTO-999-UPDATED")
                    .type("HOGAR")
                    .insurer("New Insurer")
                    .startDate(LocalDate.of(2026, 6, 1))
                    .expirationDate(LocalDate.of(2027, 6, 1))
                    .build();

            when(policyRepository.findById(100L)).thenReturn(Optional.of(testPolicy));
            when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(contactAttemptRepository.countByPolicyId(100L)).thenReturn(0);

            PolicySummaryDTO result = policyService.updatePolicy(request);

            assertThat(result.getPolicyNumber()).isEqualTo("AUTO-999-UPDATED");
            assertThat(result.getType()).isEqualTo("HOGAR");
            assertThat(result.getInsurer()).isEqualTo("New Insurer");

            verify(policyRepository).save(argThat(p ->
                    p.getPolicyNumber().equals("AUTO-999-UPDATED") &&
                    p.getType() == PolicyType.HOGAR &&
                    p.getInsurer().equals("New Insurer") &&
                    p.getStartDate().equals(LocalDate.of(2026, 6, 1)) &&
                    p.getExpirationDate().equals(LocalDate.of(2027, 6, 1))
            ));
        }

        @Test
        @DisplayName("updates only provided fields")
        void updatePolicy_partial() {
            PolicyRequestDTO request = PolicyRequestDTO.builder()
                    .id(100L)
                    .insurer("Only Insurer Changed")
                    .build();

            when(policyRepository.findById(100L)).thenReturn(Optional.of(testPolicy));
            when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(contactAttemptRepository.countByPolicyId(100L)).thenReturn(0);

            PolicySummaryDTO result = policyService.updatePolicy(request);

            assertThat(result.getInsurer()).isEqualTo("Only Insurer Changed");
            assertThat(result.getPolicyNumber()).isEqualTo("AUTO-010-2026");
            assertThat(result.getType()).isEqualTo("AUTO");

            verify(policyRepository).save(argThat(p ->
                    p.getInsurer().equals("Only Insurer Changed") &&
                    p.getPolicyNumber().equals("AUTO-010-2026") &&
                    p.getType() == PolicyType.AUTO
            ));
        }

        @Test
        @DisplayName("throws exception when policy not found")
        void updatePolicy_notFound() {
            PolicyRequestDTO request = PolicyRequestDTO.builder()
                    .id(999L)
                    .insurer("N/A")
                    .build();

            when(policyRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> policyService.updatePolicy(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Policy not found");

            verify(policyRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws exception for invalid type on update")
        void updatePolicy_invalidType() {
            PolicyRequestDTO request = PolicyRequestDTO.builder()
                    .id(100L)
                    .type("BAD_TYPE")
                    .build();

            when(policyRepository.findById(100L)).thenReturn(Optional.of(testPolicy));

            assertThatThrownBy(() -> policyService.updatePolicy(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────
    //  updateClient()
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("updateClient()")
    class UpdateClientTests {

        @Test
        @DisplayName("updates all client fields successfully")
        void updateClient_allFields() {
            ClientDetailDTO updateData = ClientDetailDTO.builder()
                    .name("Nombre Actualizado")
                    .phone("+57 999 888 7766")
                    .email("actualizado@email.com")
                    .notes("Notas actualizadas")
                    .build();

            when(clientRepository.findById(10L)).thenReturn(Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(policyRepository.findByClientId(10L)).thenReturn(List.of(testPolicy));

            ClientDetailDTO result = policyService.updateClient(10L, updateData);

            assertThat(result.getName()).isEqualTo("Nombre Actualizado");
            assertThat(result.getPhone()).isEqualTo("+57 999 888 7766");
            assertThat(result.getEmail()).isEqualTo("actualizado@email.com");
            assertThat(result.getNotes()).isEqualTo("Notas actualizadas");

            verify(clientRepository).save(argThat(c ->
                    c.getName().equals("Nombre Actualizado") &&
                    c.getPhone().equals("+57 999 888 7766") &&
                    c.getEmail().equals("actualizado@email.com") &&
                    c.getNotes().equals("Notas actualizadas")
            ));
        }

        @Test
        @DisplayName("updates only provided client fields")
        void updateClient_partial() {
            ClientDetailDTO updateData = ClientDetailDTO.builder()
                    .name("Solo Nombre")
                    .build();

            when(clientRepository.findById(10L)).thenReturn(Optional.of(testClient));
            when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(policyRepository.findByClientId(10L)).thenReturn(List.of(testPolicy));

            ClientDetailDTO result = policyService.updateClient(10L, updateData);

            assertThat(result.getName()).isEqualTo("Solo Nombre");
            assertThat(result.getPhone()).isEqualTo("+57 310 111 2233");
            assertThat(result.getEmail()).isEqualTo("juan@email.com");

            verify(clientRepository).save(argThat(c ->
                    c.getName().equals("Solo Nombre") &&
                    c.getPhone().equals("+57 310 111 2233")
            ));
        }

        @Test
        @DisplayName("throws exception when client not found")
        void updateClient_notFound() {
            ClientDetailDTO updateData = ClientDetailDTO.builder().name("X").build();

            when(clientRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> policyService.updateClient(999L, updateData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Client not found");

            verify(clientRepository, never()).save(any());
        }
    }

    // ──────────────────────────────────────────────
    //  Filter: interested / not_interested
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("Filter interested/not_interested")
    class FilterByInterestTests {

        private Policy policyWithInterest;
        private Policy policyWithNoInterest;
        private Policy policyWithContacted;

        @BeforeEach
        void setUpFilters() {
            Client c1 = Client.builder().id(1L).name("C1").phone("1").advisor(defaultAdvisor).build();
            Client c2 = Client.builder().id(2L).name("C2").phone("2").advisor(defaultAdvisor).build();
            Client c3 = Client.builder().id(3L).name("C3").phone("3").advisor(defaultAdvisor).build();

            policyWithInterest = Policy.builder()
                    .id(201L).policyNumber("P-INT-001").type(PolicyType.AUTO)
                    .insurer("I1").startDate(LocalDate.now().minusMonths(6))
                    .expirationDate(LocalDate.now().plusMonths(6))
                    .status(PolicyStatus.ACTIVO).renewalCount(0)
                    .client(c1).advisor(defaultAdvisor).build();

            policyWithNoInterest = Policy.builder()
                    .id(202L).policyNumber("P-NINT-001").type(PolicyType.HOGAR)
                    .insurer("I2").startDate(LocalDate.now().minusMonths(6))
                    .expirationDate(LocalDate.now().plusMonths(6))
                    .status(PolicyStatus.ACTIVO).renewalCount(0)
                    .client(c2).advisor(defaultAdvisor).build();

            policyWithContacted = Policy.builder()
                    .id(203L).policyNumber("P-CON-001").type(PolicyType.VIDA)
                    .insurer("I3").startDate(LocalDate.now().minusMonths(6))
                    .expirationDate(LocalDate.now().plusMonths(6))
                    .status(PolicyStatus.ACTIVO).renewalCount(0)
                    .client(c3).advisor(defaultAdvisor).build();
        }

        @Test
        @DisplayName("filter interested returns policies with last attempt = INTERESTED")
        void filterInterested() {
            when(policyRepository.findByAdvisorIdAndStatusIn(1L, List.of(PolicyStatus.ACTIVO, PolicyStatus.VENCIDO)))
                    .thenReturn(List.of(policyWithInterest, policyWithNoInterest, policyWithContacted));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(201L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(1L).date(LocalDateTime.now()).type(ContactAttemptType.CALL)
                            .result(ContactAttemptResult.INTERESTED).build()));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(202L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(2L).date(LocalDateTime.now()).type(ContactAttemptType.WHATSAPP)
                            .result(ContactAttemptResult.NOT_INTERESTED).build()));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(203L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(3L).date(LocalDateTime.now()).type(ContactAttemptType.EMAIL)
                            .result(ContactAttemptResult.CONTACTED).build()));
            when(contactAttemptRepository.countByPolicyId(anyLong())).thenReturn(1);

            List<PolicySummaryDTO> result = policyService.getPolicies(1L, "interested");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPolicyNumber()).isEqualTo("P-INT-001");
        }

        @Test
        @DisplayName("filter not_interested returns policies with last attempt = NOT_INTERESTED")
        void filterNotInterested() {
            when(policyRepository.findByAdvisorIdAndStatusIn(1L, List.of(PolicyStatus.ACTIVO, PolicyStatus.VENCIDO)))
                    .thenReturn(List.of(policyWithInterest, policyWithNoInterest, policyWithContacted));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(201L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(1L).date(LocalDateTime.now()).type(ContactAttemptType.CALL)
                            .result(ContactAttemptResult.INTERESTED).build()));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(202L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(2L).date(LocalDateTime.now()).type(ContactAttemptType.WHATSAPP)
                            .result(ContactAttemptResult.NOT_INTERESTED).build()));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(203L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(3L).date(LocalDateTime.now()).type(ContactAttemptType.EMAIL)
                            .result(ContactAttemptResult.CONTACTED).build()));
            when(contactAttemptRepository.countByPolicyId(anyLong())).thenReturn(1);

            List<PolicySummaryDTO> result = policyService.getPolicies(1L, "not_interested");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPolicyNumber()).isEqualTo("P-NINT-001");
        }

        @Test
        @DisplayName("filter interested excludes policies with no contact attempts")
        void filterInterested_noAttemptsExcluded() {
            when(policyRepository.findByAdvisorIdAndStatusIn(1L, List.of(PolicyStatus.ACTIVO, PolicyStatus.VENCIDO)))
                    .thenReturn(List.of(policyWithInterest));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(201L))
                    .thenReturn(Optional.empty());

            List<PolicySummaryDTO> result = policyService.getPolicies(1L, "interested");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter not_interested excludes policies with different last result")
        void filterNotInterested_wrongResultExcluded() {
            when(policyRepository.findByAdvisorIdAndStatusIn(1L, List.of(PolicyStatus.ACTIVO, PolicyStatus.VENCIDO)))
                    .thenReturn(List.of(policyWithInterest));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(201L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(1L).date(LocalDateTime.now()).type(ContactAttemptType.CALL)
                            .result(ContactAttemptResult.LEFT_MESSAGE).build()));

            List<PolicySummaryDTO> result = policyService.getPolicies(1L, "not_interested");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("filter interested works with multiple matching policies")
        void filterInterested_multipleMatches() {
            Policy policyWithInterest2 = Policy.builder()
                    .id(204L).policyNumber("P-INT-002").type(PolicyType.AUTO)
                    .insurer("I4").startDate(LocalDate.now().minusMonths(3))
                    .expirationDate(LocalDate.now().plusMonths(9))
                    .status(PolicyStatus.ACTIVO).renewalCount(0)
                    .client(testClient).advisor(defaultAdvisor).build();

            when(policyRepository.findByAdvisorIdAndStatusIn(1L, List.of(PolicyStatus.ACTIVO, PolicyStatus.VENCIDO)))
                    .thenReturn(List.of(policyWithInterest, policyWithInterest2, policyWithNoInterest));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(201L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(1L).date(LocalDateTime.now()).type(ContactAttemptType.CALL)
                            .result(ContactAttemptResult.INTERESTED).build()));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(204L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(2L).date(LocalDateTime.now()).type(ContactAttemptType.EMAIL)
                            .result(ContactAttemptResult.INTERESTED).build()));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(202L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(3L).date(LocalDateTime.now()).type(ContactAttemptType.WHATSAPP)
                            .result(ContactAttemptResult.NOT_INTERESTED).build()));
            when(contactAttemptRepository.countByPolicyId(anyLong())).thenReturn(1);

            List<PolicySummaryDTO> result = policyService.getPolicies(1L, "interested");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(PolicySummaryDTO::getPolicyNumber)
                    .containsExactlyInAnyOrder("P-INT-001", "P-INT-002");
        }

        @Test
        @DisplayName("filter interested includes VENCIDO policies with INTERESTED attempt")
        void filterInterested_includesVencioPolicy() {
            Policy expiredPolicy = Policy.builder()
                    .id(205L).policyNumber("P-EXP-001").type(PolicyType.AUTO)
                    .insurer("I5").startDate(LocalDate.now().minusYears(1))
                    .expirationDate(LocalDate.now().minusDays(10))
                    .status(PolicyStatus.VENCIDO).renewalCount(0)
                    .client(testClient).advisor(defaultAdvisor).build();

            when(policyRepository.findByAdvisorIdAndStatusIn(1L, List.of(PolicyStatus.ACTIVO, PolicyStatus.VENCIDO)))
                    .thenReturn(List.of(expiredPolicy));
            when(contactAttemptRepository.findTopByPolicyIdOrderByDateDesc(205L))
                    .thenReturn(Optional.of(ContactAttempt.builder()
                            .id(1L).date(LocalDateTime.now()).type(ContactAttemptType.CALL)
                            .result(ContactAttemptResult.INTERESTED).build()));
            when(contactAttemptRepository.countByPolicyId(205L)).thenReturn(1);

            List<PolicySummaryDTO> result = policyService.getPolicies(1L, "interested");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPolicyNumber()).isEqualTo("P-EXP-001");
            assertThat(result.get(0).getLastContactResult()).isEqualTo("Interesado");
        }
    }
}
