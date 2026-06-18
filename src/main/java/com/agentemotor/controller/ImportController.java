package com.agentemotor.controller;

import com.agentemotor.controller.api.ImportApi;
import com.agentemotor.dto.ImportResultDTO;
import com.agentemotor.service.ImportService;
import com.agentemotor.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController implements ImportApi {

    private final ImportService importService;

    @Override
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDTO> importFromFile(@RequestParam("file") MultipartFile file) {
        ImportResultDTO result = importService.importClients(file, AppConstants.DEFAULT_ADVISOR_ID);
        return ResponseEntity.ok(result);
    }
}
