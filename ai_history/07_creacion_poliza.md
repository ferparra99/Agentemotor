# 07_creacion_poliza.md — Endpoint y formulario para crear pólizas

## Prompt
Agregar funcionalidad para que María pueda crear nuevas pólizas desde la interfaz web.

## Implementación

### Nuevo endpoint REST: `POST /api/policies`
- Recibe JSON con: `advisorId`, `clientId`, `type`, `insurer`, `policyNumber`, `startDate`, `expirationDate`
- Crea la póliza con estado `ACTIVE` y `renewalCount = 0`
- Retorna `201 Created` con los datos de la póliza creada

### Nuevo método en `PolicyServiceImpl`: `createPolicy()`
- Valida que el asesor y el cliente existan
- Asigna `status = ACTIVE`, `renewalCount = 0`
- Guarda y retorna la póliza

### Nuevo formulario Thymeleaf: `/policy/nueva`
- Ruta GET servida por `DashboardController`
- Template: `policy-form.html` con:
  - Selector de cliente (cargado desde `ClientRepository`)
  - Campos: tipo (select con AUTO/HOGAR/VIDA), aseguradora, número de póliza, fechas de inicio y vencimiento
  - Botón de envío → POST a `/api/policies` vía JavaScript `fetch`
  - Redirección al dashboard al completar

### Archivos modificados
- `PolicyRestController.java` — nuevo método `createPolicy()`
- `PolicyServiceImpl.java` — nuevo método `createPolicy()`
- `PolicyService.java` — nueva firma `createPolicy()`
- `DashboardController.java` — nuevo endpoint `GET /policy/nueva`
- `policy-form.html` — nuevo template
