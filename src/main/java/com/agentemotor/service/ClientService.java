package com.agentemotor.service;

import com.agentemotor.dto.ClientDetailDTO;
import com.agentemotor.model.Client;
import java.util.List;

public interface ClientService {

    ClientDetailDTO getClientDetail(Long clientId);

    List<ClientDetailDTO> getAllClients(Long advisorId);

    ClientDetailDTO updateClient(Long clientId, ClientDetailDTO clientData);

    Client findOrCreateClient(String name, String phone, String email, String notes, Long advisorId);
}
