# 08_mejoras_gestion_polizas.md — Creación rápida, edición, filtros por interés y estados en español

## Prompt
Múltiples mejoras: crear cliente al vuelo al hacer póliza, editar campos existentes,
filtrar por Interesado/No interesado, pasar estados a español, mostrar interés en
prioridad, auto-refresh del panel.

## Cambios realizados

### 1. Creación de póliza con cliente nuevo
- `CreatePolicyRequestDTO` → `PolicyRequestDTO` (reemplaza `clientId` por `clientName`/`clientPhone`)
- `PolicyServiceImpl.createPolicy()` crea un `Client` automáticamente antes de crear la `Policy`
- `policy-form.html`: `<select>` de cliente → `<input type="text">` para nombre + `<input type="tel">` para teléfono
- `DashboardController.newPolicyForm()` ya no carga lista de clientes

### 2. Edición inline desde el modal
- Nuevo `PUT /api/policies/{id}` y `PUT /api/clients/{id}` en `PolicyRestController`
- `PolicyService.updatePolicy()` y `PolicyService.updateClient()` en service/impl
- Botón "Editar" en el modal que convierte campos del detalle en inputs editables (texto, fecha, select tipo)
- Botón "Guardar cambios" que envía PUT a ambos endpoints y refresca

### 3. Filtros "Interesados" / "No interesados"
- Nuevos casos en `PolicyServiceImpl.filterPolicies()`: `"interested"` y `"not_interested"`
- Lógica: filtra pólizas activas cuyo último `ContactAttempt` tenga el resultado indicado
- Nuevo método en `ContactAttemptRepository`: `findTopByPolicyIdOrderByDateDesc()`
- Botones en el dashboard junto a los filtros existentes

### 4. Pruebas unitarias con Mockito
- `PolicyServiceImplTest.java` — 17 tests con `@ExtendWith(MockitoExtension.class)`
  - `createPolicy()`: 4 tests (éxito, sin teléfono, advisor no encontrado, tipo inválido)
  - `updatePolicy()`: 4 tests (completo, parcial, no encontrado, tipo inválido)
  - `updateClient()`: 3 tests (completo, parcial, no encontrado)
  - `Filter interested/not_interested`: 6 tests (filtro, sin intentos, múltiples, vencida, etc.)

### 5. Centralización de constantes
- Nuevo `utils/PolicyConstants.java` con constantes agrupadas:
  - IDs por defecto, días de ventana regulatoria, mensajes de error, prioridades, acciones recomendadas
- `PolicyServiceImpl.java` y `DashboardController.java` actualizados para usar las constantes

### 6. Estados de póliza en español
- `PolicyStatus` renombrado: `ACTIVE` → `ACTIVA`, `EXPIRED` → `VENCIDA`, `RENEWED` → `RENOVADA`
- Actualizado en: `Policy.java`, `PolicyServiceImpl.java`, `data.sql`, `dashboard.html` (CSS y JS)
- Se eliminó `agentemotor.db` para regenerar con nuevos valores

### 7. Interés en columna de prioridad
- Nuevo campo `interestStatus` en `PolicySummaryDTO`
- En `toSummaryDTO()` se consulta el último `ContactAttempt`; si es INTERESTED/NOT_INTERESTED se muestra como badge
- CSS: `.badge.interesado` (naranja claro), `.badge.no-interesado` (gris)

### 8. Auto-refresh al cerrar modal
- `closeModal()` ahora ejecuta `refreshTable()` para actualizar la lista sin recargar

## Archivos modificados
- `src/main/java/.../dto/PolicyRequestDTO.java` — nuevo (reemplaza CreatePolicyRequestDTO)
- `src/main/java/.../dto/PolicySummaryDTO.java` — + interestStatus
- `src/main/java/.../dto/PolicyDetailDTO.java` — + clientId
- `src/main/java/.../model/PolicyStatus.java` — renombrado a español
- `src/main/java/.../repository/ContactAttemptRepository.java` — + findTopBy...()
- `src/main/java/.../service/PolicyService.java` — + updatePolicy, updateClient
- `src/main/java/.../service/PolicyServiceImpl.java` — múltiples cambios
- `src/main/java/.../controller/PolicyRestController.java` — + PUT endpoints
- `src/main/java/.../controller/DashboardController.java` — usa PolicyConstants
- `src/main/java/.../utils/PolicyConstants.java` — nuevo
- `src/main/resources/templates/policy-form.html` — inputs texto
- `src/main/resources/templates/dashboard.html` — filtros, edición, CSS, refresh
- `src/main/resources/data.sql` — estados en español
- `src/test/java/.../service/PolicyServiceImplTest.java` — nuevo (17 tests)
- `src/test/java/.../service/PolicyServiceTest.java` — estados en español
- `CreatePolicyRequestDTO.java` — eliminado

## Pruebas
- 21 tests en total, todos pasan: 4 existentes + 17 nuevos
