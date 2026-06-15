package com.agentemotor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Resultado de una operación de importación masiva")
public class ImportResultDTO {

    @Schema(description = "Total de registros procesados (válidos + errores)", example = "10")
    private int totalProcesados;

    @Schema(description = "Total de registros con error", example = "1")
    private int totalErrores;

    @Schema(description = "Total de registros creados exitosamente", example = "9")
    private int totalCreados;

    @Schema(description = "Lista de mensajes de error por fila", example = "[\"Fila 5: datos inválidos - Campos requeridos faltantes: tipo\"]")
    private List<String> errores;
}
