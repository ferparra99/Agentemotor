package com.agentemotor.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "AgenteMotor API",
        version = "1.0",
        description = """
            API de gestión de pólizas de seguro — AgenteMotor.
            
            Permite administrar pólizas, clientes, intentos de contacto e importación masiva desde archivos Excel/XML.
            
            ---
            ## Filtros disponibles para GET /api/policies/list
            | Filtro | Descripción |
            |--------|-------------|
            | `all` | Todas las pólizas |
            | `active` | Solo activas |
            | `expiring` | Próximas a vencer (30 días) |
            | `expired_lt_30` | Vencidas hace menos de 30 días |
            | `expired_gt_30` | Vencidas hace más de 30 días |
            | `interested` | Cliente interesado |
            | `not_interested` | Cliente no interesado |
            
            ---
            ## Enumeraciones
            ### PolicyType
            | Valor | Descripción |
            |-------|-------------|
            | `AUTO` | Seguro de automóvil |
            | `HOGAR` | Seguro de hogar |
            | `VIDA` | Seguro de vida |
            
            ### PolicyStatus
            | Valor | Descripción |
            |-------|-------------|
            | `ACTIVO` | Póliza vigente |
            | `VENCIDO` | Póliza vencida |
            | `PERDIDO` | Cliente perdido (fuera de ventana de renovación) |
            | `RENOVADA` | Póliza renovada |
            
            ### ContactAttemptType
            | Valor | Descripción |
            |-------|-------------|
            | `CALL` | Llamada telefónica |
            | `EMAIL` | Correo electrónico |
            | `WHATSAPP` | Mensaje de WhatsApp |
            
            ### ContactAttemptResult
            | Valor | Descripción |
            |-------|-------------|
            | `CONTACTED` | Cliente contactado con éxito |
            | `NO_ANSWER` | No contestó |
            | `LEFT_MESSAGE` | Se dejó mensaje |
            | `INTERESTED` | Cliente interesado en renovar |
            | `NOT_INTERESTED` | Cliente no interesado en renovar
            """,
        contact = @Contact(name = "Soporte AgenteMotor"),
        license = @License(name = "Apache 2.0")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local de desarrollo")
    }
)
public class OpenApiConfig {}
