package com.agentemotor.controller;

import com.agentemotor.dto.ClientDetailDTO;
import com.agentemotor.service.ClientService;
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
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes — consulta de lista, detalle y actualización de datos")
public class ClientController {

    private final ClientService clientService;

    @Operation(
        summary = "Listar clientes por asesor",
        description = """
            Retorna todos los clientes asociados a un asesor, incluyendo el conteo de pólizas activas y totales.

            **Ejemplo de consumo:**
            ```
            GET /api/clients/list?advisorId=1
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos (advisorId requerido)")
    })
    @GetMapping("/list")
    public ResponseEntity<List<ClientDetailDTO>> listByAdvisor(
            @Parameter(description = "ID del asesor", required = true, example = "1")
            @RequestParam Long advisorId) {
        return ResponseEntity.ok(clientService.getAllClients(advisorId));
    }

    @Operation(
        summary = "Obtener detalle de un cliente",
        description = """
            Retorna el detalle completo de un cliente, incluyendo datos de contacto y conteo de pólizas.

            **Ejemplo de consumo:**
            ```
            GET /api/clients/detail/1
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle del cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/detail/{id}")
    public ResponseEntity<ClientDetailDTO> getById(
            @Parameter(description = "ID del cliente", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientDetail(id));
    }

    @Operation(
        summary = "Actualizar datos de un cliente",
        description = """
            Actualiza los datos de un cliente existente (nombre, teléfono, email, notas).

            **Ejemplo de consumo:**
            ```
            PUT /api/clients/update/1
            Content-Type: application/json

            {
              "name": "Juan Pérez",
              "phone": "555-0200",
              "email": "juan.perez@example.com",
              "notes": "Cliente actualizado"
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<ClientDetailDTO> update(
            @Parameter(description = "ID del cliente a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados del cliente", required = true)
            @RequestBody ClientDetailDTO clientData) {
        return ResponseEntity.ok(clientService.updateClient(id, clientData));
    }
}
