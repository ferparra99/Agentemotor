package com.agentemotor.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImportResultDTO {
    private int totalProcesados;
    private int totalErrores;
    private int totalCreados;
    private List<String> errores;
}
