package com.agentemotor.controller;

import com.agentemotor.dto.ImportResultDTO;
import com.agentemotor.service.ImportService;
import com.agentemotor.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
@Tag(name = "Importaciones", description = "Importación masiva de pólizas y clientes desde archivos Excel (.xlsx) o XML")
public class ImportController {

    private final ImportService importService;

    @Operation(
        summary = "Importar pólizas desde archivo",
        description = """
            Importa pólizas y clientes de forma masiva desde un archivo Excel (.xlsx) o XML.
            El archivo se envía como multipart/form-data.

            **Formato esperado del Excel (columnas):**
            | nombre | número de póliza | tipo | aseguradora | fecha de inicio | fecha de vencimiento | teléfono | email | notas |
            |--------|-----------------|------|-------------|-----------------|---------------------|----------|-------|-------|

            **Formato esperado del XML:**
            ```xml
            <Policies>
              <Policy>
                <nombre>Juan Pérez</nombre>
                <numeroPoliza>POL-001</numeroPoliza>
                <tipo>AUTO</tipo>
                <aseguradora>Mapfre</aseguradora>
                <fechaInicio>2026-01-01</fechaInicio>
                <fechaVencimiento>2026-12-31</fechaVencimiento>
                <telefono>555-0100</telefono>
                <email>juan@example.com</email>
                <notas>Cliente preferencial</notas>
              </Policy>
            </Policies>
            ```

            **Campos requeridos por fila:** nombre, número de póliza, tipo, aseguradora, fecha de inicio, fecha de vencimiento

            **Tipos de póliza aceptados:** `AUTO`, `HOGAR`, `VIDA`

            **Ejemplo de consumo (cURL):**
            ```bash
            curl -X POST http://localhost:8080/api/imports/upload \\
              -F "file=@polizas.xlsx"
            ```

            **Respuesta:**
            ```json
            {
              "totalProcesados": 10,
              "totalErrores": 1,
              "totalCreados": 9,
              "errores": ["Fila 5: datos inválidos - Campos requeridos faltantes: tipo"]
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Importación procesada (ver resultados en el cuerpo)"),
        @ApiResponse(responseCode = "400", description = "Archivo vacío, sin nombre o formato no soportado")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDTO> importFromFile(
            @Parameter(description = "Archivo Excel (.xlsx) o XML con los datos de pólizas", required = true)
            @RequestParam("file") MultipartFile file) {
        ImportResultDTO result = importService.importClients(file, AppConstants.DEFAULT_ADVISOR_ID);
        return ResponseEntity.ok(result);
    }
}
