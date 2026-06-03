# Sesión 10: Columna "Última gestión" y gestión para todos los estados

## Cambios realizados

### 1. `PolicyRepository.java`
- Agregado `findByAdvisorIdAndStatusIn(Long advisorId, List<PolicyStatus> statuses)` para consultar múltiples estados

### 2. `PolicySummaryDTO.java`
- Agregado campo `lastContactResult` (String) con el texto en español de la última gestión

### 3. `PolicyServiceImpl.java`
- `filterPolicies()`: filtros `interested`/`not_interested` ahora consultan `ACTIVO` y `VENCIDO` (antes solo `ACTIVO`)
- `toSummaryDTO()`: nueva lógica que mapea `ContactAttemptResult` → texto español:
  - CONTACTED → "Contactado"
  - NO_ANSWER → "No contestó"
  - LEFT_MESSAGE → "Dejó mensaje"
  - INTERESTED → "Interesado"
  - NOT_INTERESTED → "No interesado"
  - Sin intentos → "No contactado" (default)

### 4. `dashboard.html`
- Columnas de tabla divididas: "Gestiones" (conteo) + "Última gestión" (badge con color)
- Eliminado badge de interés de la columna Prioridad
- Modal: acciones (registrar gestión, renovar) y edición disponibles para TODOS los estados (ACTIVO, VENCIDO, PERDIDO, RENOVADA)
- CSS: badges de color para cada resultado de gestión

### 5. Tests
- Mocks actualizados: `findByAdvisorIdAndStatus` → `findByAdvisorIdAndStatusIn`
- Test `filterInterested_expiredPolicyExcluded` → `filterInterested_includesVencioPolicy`

### 6. Documentación
- README.md y spec.md actualizados con nueva estructura de columnas

## Archivos modificados
- `src/main/java/com/agentemotor/repository/PolicyRepository.java`
- `src/main/java/com/agentemotor/dto/PolicySummaryDTO.java`
- `src/main/java/com/agentemotor/service/PolicyServiceImpl.java`
- `src/main/resources/templates/dashboard.html`
- `src/test/java/com/agentemotor/service/PolicyServiceImplTest.java`
- `README.md`
- `spec.md`
- `ai_history/10_ultima_gestion_columna.md`
