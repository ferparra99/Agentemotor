package com.agentemotor.service;

import com.agentemotor.dto.ImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {
    ImportResultDTO importClients(MultipartFile file, Long advisorId);
}
