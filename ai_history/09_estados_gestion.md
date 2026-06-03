# Sesión 09: Estados de póliza ACTIVO/VENCIDO/PERDIDO/RENOVADA

## Problema
Los estados de póliza (ACTIVA, VENCIDA, RENOVADA) no reflejaban la ventana regulatoria de 30 días de Colombia, y no había un estado `PERDIDO` explícito para pólizas vencidas >30 días.

## Cambios realizados

### 1. `PolicyStatus.java`
- Renombrado: `ACTIVA` → `ACTIVO`, `VENCIDA` → `VENCIDO`
- Agregado: `PERDIDO`
- Conservado: `RENOVADA`

### 2. `Policy.java`
- Default status: `PolicyStatus.ACTIVA` → `PolicyStatus.ACTIVO`

### 3. `PolicyServiceImpl.java`
- Nuevo método `updatePolicyStatuses(Long advisorId)`: recorre las pólizas `ACTIVO` y las clasifica:
  - Vencidas > 30d → `PERDIDO`
  - Vencidas ≤ 30d → `VENCIDO`
  - Vigentes → `ACTIVO` (sin cambio)
- Se llama desde `getPolicies()` y `getDashboardStats()` (antes de cualquier lógica)
- `filterPolicies()`:
  - `active` → `findByAdvisorIdAndStatus(advisorId, PolicyStatus.ACTIVO)`
  - `expired_lt_30` → `findByAdvisorIdAndStatus(advisorId, PolicyStatus.VENCIDO)`
  - `expired_gt_30` → `findByAdvisorIdAndStatus(advisorId, PolicyStatus.PERDIDO)`
  - `interested`/`not_interested` → siempre filtra sobre `ACTIVO`
- `getDashboardStats()`: cuenta por status `ACTIVO`, `VENCIDO`, `PERDIDO`, `RENOVADA`
- `calculatePriority()`:
  - `RENOVADA` → "completada"
  - `PERDIDO` → "perdido"
  - `VENCIDO` → "urgente"
  - `ACTIVO` → "alta"/"media"/"baja" según días restantes
- `calculateRecommendedAction()`:
  - `RENOVADA`/`PERDIDO` → "Póliza ya gestionada"
  - `VENCIDO` → mensaje urgente con días restantes
  - `ACTIVO` → "Gestionar renovación antes del vencimiento"

### 4. `data.sql`
- Seed data actualizada: pólizas 5 y 6 → `VENCIDO`, póliza 7 → `PERDIDO`, pólizas 1-4 → `ACTIVO`

### 5. `dashboard.html`
- CSS: `.badge.activa` → `.badge.activo`, `.badge.vencida` → `.badge.vencido`; se conserva `.badge.perdido`
- JS: `d.status === 'ACTIVA'` → `'ACTIVO'` (2 ocurrencias)

### 6. Tests
- `PolicyServiceTest.java`: `PolicyStatus.ACTIVA` → `ACTIVO`; prioridad de vencida 5d pasa de "alta" a "urgente"
- `PolicyServiceImplTest.java`: `PolicyStatus.ACTIVA` → `ACTIVO`, `PolicyStatus.VENCIDA` → `VENCIDO`
- 21 tests pasan

### 7. Documentación
- `README.md`: tabla de prioridades, PolicyStatus enum, línea de decisión de diseño
- `spec.md`: enum de status, tabla de prioridades

## Archivos modificados
- `src/main/java/com/agentemotor/model/PolicyStatus.java`
- `src/main/java/com/agentemotor/model/Policy.java`
- `src/main/java/com/agentemotor/service/PolicyServiceImpl.java`
- `src/main/resources/data.sql`
- `src/main/resources/templates/dashboard.html`
- `src/test/java/com/agentemotor/service/PolicyServiceTest.java`
- `src/test/java/com/agentemotor/service/PolicyServiceImplTest.java`
- `README.md`
- `spec.md`

## Notas
- Se eliminó `agentemotor.db` para forzar regeneración desde `data.sql`
- La lógica de `updatePolicyStatuses()` se ejecuta en cada `getPolicies()` y `getDashboardStats()`, asegurando que los estados estén siempre actualizados sin necesidad de un job programado
```