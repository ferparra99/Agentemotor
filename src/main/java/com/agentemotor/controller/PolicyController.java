package com.agentemotor.controller;

import com.agentemotor.dto.*;
import com.agentemotor.service.ContactAttemptService;
import com.agentemotor.service.PolicyService;
import com.agentemotor.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Pólizas", description = "Gestión de pólizas — CRUD, renovación, intentos de contacto y filtros de estado")
public class PolicyController {

    private final PolicyService policyService;
    private final ContactAttemptService contactAttemptService;

    @Operation(
        summary = "Listar pólizas por asesor con filtro",
        description = """
            Retorna las pólizas de un asesor aplicando un filtro de estado.

            **Filtros disponibles:**
            - `all` — Todas las pólizas
            - `active` — Solo activas
            - `expiring` — Próximas a vencer (próximos 30 días)
            - `expired_lt_30` — Vencidas hace menos de 30 días
            - `expired_gt_30` — Vencidas hace más de 30 días
            - `interested` — Cliente interesado en renovar
            - `not_interested` — Cliente no interesado en renovar

            **Ejemplo de consumo:**
            ```
            GET /api/policies/list?advisorId=1&filter=expiring
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pólizas filtrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos (advisorId requerido)")
    })
    @GetMapping("/list")
    public ResponseEntity<List<PolicySummaryDTO>> listByAdvisorAndFilter(
            @Parameter(description = "ID del asesor", required = true, example = "1")
            @RequestParam Long advisorId,
            @Parameter(description = "Filtro de estado (all, active, expiring, expired_lt_30, expired_gt_30, interested, not_interested)", example = "all")
            @RequestParam(defaultValue = AppConstants.FILTER_ALL) String filter) {
        return ResponseEntity.ok(policyService.getPolicies(advisorId, filter));
    }

    @Operation(
        summary = "Obtener detalle de una póliza",
        description = """
            Retorna el detalle completo de una póliza, incluyendo datos del cliente y el historial
            de intentos de contacto.

            **Ejemplo de consumo:**
            ```
            GET /api/policies/detail/1
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle de la póliza encontrado"),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    @GetMapping("/detail/{id}")
    public ResponseEntity<PolicyDetailDTO> getById(
            @Parameter(description = "ID de la póliza", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyDetail(id));
    }

    @Operation(
        summary = "Crear una nueva póliza",
        description = """
            Crea una nueva póliza junto con un cliente (si no existe, se crea automáticamente).

            **Ejemplo de cuerpo de solicitud:**
            ```json
            {
              "policyNumber": "POL-001",
              "type": "AUTO",
              "insurer": "Mapfre",
              "startDate": "2026-01-01",
              "expirationDate": "2026-12-31",
              "clientName": "Juan Pérez",
              "clientPhone": "555-0100",
              "clientEmail": "juan@example.com",
              "clientNotes": "Cliente preferencial"
            }
            ```

            **Tipos de póliza válidos:** `AUTO`, `HOGAR`, `VIDA`
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Póliza creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo de póliza incorrecto")
    })
    @PostMapping("/create")
    public ResponseEntity<PolicySummaryDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la póliza a crear", required = true)
            @RequestBody PolicyRequestDTO request) {
        return ResponseEntity.ok(policyService.createPolicy(request));
    }

    @Operation(
        summary = "Actualizar una póliza existente",
        description = """
            Actualiza los datos de una póliza existente. El ID se toma del path参数.

            **Ejemplo de consumo:**
            ```
            PUT /api/policies/update/1
            ```
            **Cuerpo:** mismo formato que POST /create
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Póliza actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<PolicySummaryDTO> update(
            @Parameter(description = "ID de la póliza a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados de la póliza", required = true)
            @RequestBody PolicyRequestDTO request) {
        request.setId(id);
        return ResponseEntity.ok(policyService.updatePolicy(request));
    }

    @Operation(
        summary = "Renovar una póliza",
        description = """
            Renueva una póliza existente. Si la póliza está vencida y es de tipo AUTO,
            se puede renovar dentro de la ventana de renovación (30 días después del vencimiento).

            **Ejemplo de consumo:**
            ```
            PUT /api/policies/renew/1
            Content-Type: application/json

            {
              "newExpirationDate": "2027-12-31"
            }
            ```

            **Reglas:**
            - Pólizas activas → se renuevan sumando 12 meses a la nueva fecha
            - Pólizas vencidas tipo AUTO → se renuevan si están dentro de la ventana de 30 días
            - Pólizas vencidas HOGAR/VIDA o fuera de ventana → error
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Póliza renovada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Póliza no renovable (tipo no AUTO fuera de ventana)"),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    @PutMapping("/renew/{id}")
    public ResponseEntity<PolicySummaryDTO> renew(
            @Parameter(description = "ID de la póliza a renovar", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nueva fecha de vencimiento", required = true)
            @RequestBody RenewRequestDTO request) {
        return ResponseEntity.ok(policyService.renewPolicy(id, request));
    }

    @Operation(
        summary = "Registrar intento de contacto",
        description = """
            Registra un intento de contacto para una póliza específica.

            **Ejemplo de consumo:**
            ```
            POST /api/policies/register-attempt/1
            Content-Type: application/json

            {
              "type": "CALL",
              "result": "INTERESTED",
              "notes": "Cliente interesado en renovar, llamar la próxima semana"
            }
            ```

            **Tipos de contacto (`type`):** `CALL`, `EMAIL`, `WHATSAPP`

            **Resultados de contacto (`result`):**
            - `CONTACTED` — Contacto exitoso
            - `NO_ANSWER` — No contestó
            - `LEFT_MESSAGE` — Se dejó mensaje
            - `INTERESTED` — Cliente interesado
            - `NOT_INTERESTED` — Cliente no interesado
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Intento de contacto registrado"),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    @PostMapping("/register-attempt/{id}")
    public ResponseEntity<ContactAttemptDTO> registerAttempt(
            @Parameter(description = "ID de la póliza", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos del intento de contacto", required = true)
            @RequestBody ContactAttemptRequestDTO request) {
        request.setPolicyId(id);
        return ResponseEntity.ok(contactAttemptService.registerContactAttempt(request));
    }
}
