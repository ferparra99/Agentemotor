package com.agentemotor.service;

import com.agentemotor.dto.ClientDetailDTO;
import com.agentemotor.model.*;
import com.agentemotor.repository.AdvisorRepository;
import com.agentemotor.repository.ClientRepository;
import com.agentemotor.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AdvisorRepository advisorRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Advisor defaultAdvisor;
    private Client testClient;

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
            when(policyRepository.findByClientId(10L)).thenReturn(List.of());

            ClientDetailDTO result = clientService.updateClient(10L, updateData);

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
            when(policyRepository.findByClientId(10L)).thenReturn(List.of());

            ClientDetailDTO result = clientService.updateClient(10L, updateData);

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

            assertThatThrownBy(() -> clientService.updateClient(999L, updateData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Client not found");

            verify(clientRepository, never()).save(any());
        }
    }

    // ──────────────────────────────────────────────
    //  findOrCreateClient()
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("findOrCreateClient()")
    class FindOrCreateClientTests {

        @Test
        @DisplayName("returns existing client when found by name and phone")
        void findOrCreateClient_returnsExisting() {
            when(clientRepository.findByAdvisorIdAndNameAndPhone(1L, "Juan Pérez", "+57 310 111 2233"))
                    .thenReturn(Optional.of(testClient));

            Client result = clientService.findOrCreateClient("Juan Pérez", "+57 310 111 2233", null, null, 1L);

            assertThat(result).isSameAs(testClient);
            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates new client when not found")
        void findOrCreateClient_createsNew() {
            Client newClient = Client.builder()
                    .id(99L)
                    .name("Nuevo Cliente")
                    .phone("+57 300 999 8877")
                    .advisor(defaultAdvisor)
                    .build();

            when(clientRepository.findByAdvisorIdAndNameAndPhone(1L, "Nuevo Cliente", "+57 300 999 8877"))
                    .thenReturn(Optional.empty());
            when(advisorRepository.findById(1L)).thenReturn(Optional.of(defaultAdvisor));
            when(clientRepository.save(any(Client.class))).thenReturn(newClient);

            Client result = clientService.findOrCreateClient("Nuevo Cliente", "+57 300 999 8877", null, null, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Nuevo Cliente");
        }
    }
}
