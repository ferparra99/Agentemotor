package com.agentemotor.controller;

import com.agentemotor.controller.api.ClientApi;
import com.agentemotor.dto.ClientDetailDTO;
import com.agentemotor.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController implements ClientApi {

    private final ClientService clientService;

    @Override
    @GetMapping("/list")
    public ResponseEntity<List<ClientDetailDTO>> listByAdvisor(@RequestParam Long advisorId) {
        return ResponseEntity.ok(clientService.getAllClients(advisorId));
    }

    @Override
    @GetMapping("/detail/{id}")
    public ResponseEntity<ClientDetailDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientDetail(id));
    }

    @Override
    @PutMapping("/update/{id}")
    public ResponseEntity<ClientDetailDTO> update(@PathVariable Long id, @RequestBody ClientDetailDTO clientData) {
        return ResponseEntity.ok(clientService.updateClient(id, clientData));
    }
}
