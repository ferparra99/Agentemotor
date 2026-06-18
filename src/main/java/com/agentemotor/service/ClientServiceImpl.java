package com.agentemotor.service;

import com.agentemotor.dto.ClientDetailDTO;
import com.agentemotor.model.*;
import com.agentemotor.repository.AdvisorRepository;
import com.agentemotor.repository.ClientRepository;
import com.agentemotor.repository.PolicyRepository;
import com.agentemotor.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final AdvisorRepository advisorRepository;
    private final PolicyRepository policyRepository;

    @Override
    @Transactional(readOnly = true)
    public ClientDetailDTO getClientDetail(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.CLIENT_NOT_FOUND + clientId));
        return buildClientDetailDTO(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDetailDTO> getAllClients(Long advisorId) {
        return clientRepository.findByAdvisorId(advisorId).stream()
                .map(this::buildClientDetailDTO)
                .toList();
    }

    @Override
    @Transactional
    public ClientDetailDTO updateClient(Long clientId, ClientDetailDTO clientData) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.CLIENT_NOT_FOUND + clientId));

        if (clientData.getName() != null) client.setName(clientData.getName());
        if (clientData.getPhone() != null) client.setPhone(clientData.getPhone());
        if (clientData.getEmail() != null) client.setEmail(clientData.getEmail());
        if (clientData.getNotes() != null) client.setNotes(clientData.getNotes());

        client = clientRepository.save(client);

        return buildClientDetailDTO(client);
    }

    @Override
    @Transactional
    public Client findOrCreateClient(String name, String phone, String email, String notes, Long advisorId) {
        Optional<Client> existing = clientRepository.findByAdvisorIdAndNameAndPhone(advisorId, name, phone);
        if (existing.isPresent()) {
            Client client = existing.get();
            if (email != null && client.getEmail() == null) client.setEmail(email);
            if (notes != null && client.getNotes() == null) client.setNotes(notes);
            return client;
        }

        Advisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new IllegalArgumentException(AppConstants.ADVISOR_NOT_FOUND));

        Client newClient = Client.builder()
                .name(name)
                .phone(phone != null ? phone : AppConstants.DEFAULT_PHONE)
                .email(email)
                .notes(notes)
                .advisor(advisor)
                .build();
        return clientRepository.save(newClient);
    }

    private ClientDetailDTO buildClientDetailDTO(Client client) {
        List<Policy> allPolicies = policyRepository.findByClientId(client.getId());
        long activeCount = allPolicies.stream()
                .filter(p -> p.getStatus() == PolicyStatus.ACTIVO)
                .count();

        return ClientDetailDTO.builder()
                .id(client.getId())
                .name(client.getName())
                .phone(client.getPhone())
                .email(client.getEmail())
                .notes(client.getNotes())
                .activePolicies((int) activeCount)
                .totalPolicies(allPolicies.size())
                .build();
    }
}
