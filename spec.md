# Spec — Agentemotor: Panel de Gestión para Asesores de Seguros

## 1. Entendimiento del Problema

María, una asesora de seguros con 280 clientes activos, usa un Excel para gestionar
las pólizas de su cartera. Cada lunes filtra las pólizas que vencen ese mes, llama
cliente por cliente, marca una columna "gestionado" con una X, y cuando renuevan
actualiza la fecha. Su flujo tiene problemas concretos:

- **El Excel se daña, se duplica, se pierde contexto** — no hay un registro
  histórico confiable.
- **Pérdida de clientes por vencimientos no detectados** — 5 a 10 clientes al mes
  se van con otro asesor porque María no identificó a tiempo que una póliza había
  vencido.
- **Sin trazabilidad** — no queda registro de qué se le ofreció a cada cliente ni
  cuándo se contactó.

**Contexto regulatorio crítico**: En Colombia, las pólizas de auto tienen una
ventana de 30 días post-vencimiento para renovar sin perder el historial. Pasados
los 30 días, la renovación se trata como nueva contratación y el asesor compite
con cualquier otro intermediario.

## 2. Decisiones de Construcción

### Construido

| Funcionalidad | Justificación |
|---|---|
| Dashboard con resumen de pólizas por estado | Reemplaza la vista principal del Excel de María |
| Filtros: todas, vigentes, por vencer, vencidas <30d, vencidas >30d, **Interesados**, **No interesados** | Refleja la ventana regulatoria + permite segmentar por intención de compra |
| Vista detalle de póliza con historial de gestiones | Reemplaza la columna "gestionado" del Excel |
| Registro de intentos de contacto (tipo, resultado, notas) | Da trazabilidad a las acciones de María |
| Acción de renovar póliza con nueva fecha | Reemplaza la actualización manual en el Excel |
| Priorización automática en español (perdido/urgente/alta/media/baja/completada) | Ayuda a María a enfocarse en lo crítico |
| Creación de póliza con cliente nuevo | María escribe nombre y teléfono; el sistema crea el cliente automáticamente |
| Edición inline desde el modal | Permite corregir datos de cliente y póliza sin salir de la gestión |
| Estado de la última gestión visible | Columna "Última gestión" con color (Contactado, No contestó, Dejó mensaje, Interesado, No interesado, No contactado) |
| Auto-refresh al cerrar modal | La tabla se actualiza sin recargar la página |
| Botón "Refrescar" en dashboard | Recarga la página manteniendo el filtro activo |
| API REST completa | Permite integración futura |
| Swagger/OpenAPI disponible en `/swagger-ui.html` | Documentación interactiva de la API |
| Seed data precargada con escenarios reales | La app funciona desde el primer arranque |
| Importación masiva desde Excel (.xlsx) y XML | Reemplaza la migración manual de datos desde Excel |
| Validación de renovación (solo AUTO post-vencimiento, máx 30 días) | Refleja la ventana regulatoria colombiana |
| Logging estructurado con SLF4J | Trazabilidad de operaciones en servidor |
| 23 tests (5 integración + 17 unitarios + 1 context) | Validación del core business y nuevas funcionalidades |

### No Construido (y por qué)

| Funcionalidad | Razón |
|---|---|
| Autenticación de usuarios | Prueba de concepto con un solo asesor. Aporta fricción sin valor para el alcance |
| Multi-asesor | María es la única usuaria. El ID del asesor está en `PolicyConstants.DEFAULT_ADVISOR_ID` |
| Notificaciones automáticas (email, WhatsApp) | Excede el tiempo estimado. Se reemplaza con la priorización visual |
| Reportes y estadísticas avanzadas | María necesita gestionar, no analizar. El dashboard básico es suficiente |
| Roles y permisos | No hay multi-usuario |
| Historial de pólizas anteriores al sistema | Se empieza desde cero con los datos de María |

## 3. Supuestos

1. **María es la asesora por defecto** — ID = `PolicyConstants.DEFAULT_ADVISOR_ID` (1L).
   Sin login. En producción se agregaría autenticación.
2. **Una renovación crea una nueva póliza** — La original pasa a estado `RENOVADA`
   y se crea una nueva `ACTIVA` con la fecha extendida, manteniendo el historial
   completo.
3. **La ventana de 30 días aplica a todos los tipos de póliza** aunque el contexto
   regulatorio menciona específicamente autos. El sistema es genérico.
4. **Las aseguradoras son texto libre** — no hay tabla maestra de aseguradoras.
   Suficiente para el MVP.
5. **Los contactos se registran con timestamp automático** — la asesora no necesita
   ingresar la fecha manualmente.
6. **La base de datos SQLite se crea en el directorio de trabajo** — `agentemotor.db`
   aparece junto al JAR.
7. **Los valores de enum se almacenan como string en SQLite** — cambiarlos (ej:
   `ACTIVE` → `ACTIVA`) requiere borrar `agentemotor.db` para regenerar.

## 4. Flujos Principales

### Flujo 1: Dashboard diario
1. María abre `http://localhost:8080/`
2. Ve las tarjetas de resumen: activas, vencen esta semana, vencidas <30d,
   vencidas >30d, renovadas
3. La tabla muestra: cliente, póliza, aseguradora, tipo, vencimiento, estado,
   prioridad, gestiones (conteo) y última gestión (badge con color)
4. Puede filtrar por estado usando los botones de filtro, incluyendo
   "Interesados" y "No interesados" (incluye pólizas VENCIDO y ACTIVO)

### Flujo 2: Gestión de una póliza
1. María da clic en "Gestionar" en cualquier póliza
2. Se abre un modal con detalle completo: cliente, póliza, historial de gestiones
4. Puede registrar un nuevo intento de contacto (tipo, resultado, notas) en
   cualquier póliza, independientemente de su estado (ACTIVO, VENCIDO, PERDIDO, RENOVADA)
5. Al cerrar el modal, la tabla se actualiza automáticamente

### Flujo 3: Renovación
1. En el modal de detalle, María da clic en "Renovar póliza"
2. Ingresa la nueva fecha de vencimiento
3. El sistema valida la restricción regulatoria:
   - Si la póliza está vencida y **no es AUTO**, se rechaza (solo autos pueden renovar post-vencimiento)
   - Si la póliza está vencida **más de 30 días**, se rechaza (ventana regulatoria expirada)
   - En ambos casos, la UI oculta el botón y muestra el mensaje de restricción
4. Si pasa validación, el sistema marca la original como `RENOVADA` y crea una nueva `ACTIVA`
5. Al cerrar el modal, el dashboard se actualiza

### Flujo 4: Crear póliza con cliente nuevo
1. María da clic en "+ Nueva Póliza"
2. Completa: nombre del cliente, teléfono, número de póliza, tipo, aseguradora,
   fechas de inicio y vencimiento
3. El sistema crea el cliente automáticamente y luego la póliza
4. Es redirigida al dashboard con los datos actualizados

### Flujo 5: Editar campos desde el modal
1. En el modal de detalle, María da clic en "Editar"
2. Los campos de cliente (nombre, teléfono) y póliza (número, tipo, aseguradora,
   vencimiento) se vuelven editables
3. Modifica los datos y da clic en "Guardar cambios"
4. El modal se actualiza y el dashboard refleja los cambios

## 5. Modelo de Datos

```
Advisor
  id: Long (PK)
  name: String
  email: String
  phone: String

Client
  id: Long (PK)
  name: String
  phone: String
  email: String (nullable)
  notes: TEXT (nullable)
  advisor_id: Long (FK → Advisor)

Policy
  id: Long (PK)
  policy_number: String
  type: Enum [AUTO, HOGAR, VIDA]
  insurer: String
  start_date: LocalDate
  expiration_date: LocalDate
  status: Enum [ACTIVO, VENCIDO, PERDIDO, RENOVADA]
  renewal_count: Integer
  client_id: Long (FK → Client)
  advisor_id: Long (FK → Advisor)

ContactAttempt
  id: Long (PK)
  date: LocalDateTime
  type: Enum [CALL, EMAIL, WHATSAPP]
  result: Enum [CONTACTED, NO_ANSWER, LEFT_MESSAGE, INTERESTED, NOT_INTERESTED]
  notes: TEXT (nullable)
  policy_id: Long (FK → Policy)
```

### Prioridades (lógica de negocio)

| Prioridad | Condición (status / días) |
|-----------|--------------------------|
| perdido | Status `PERDIDO` (vencido > 30 días) |
| urgente | Status `VENCIDO` (vencido ≤ 30 días) |
| alta | Status `ACTIVO` y vence en ≤ 7 días |
| media | Status `ACTIVO` y vence en ≤ 30 días |
| baja | Status `ACTIVO` y vence en > 30 días |
| completada | Status `RENOVADA` |

El campo `lastContactResult` en `PolicySummaryDTO` muestra el texto en español del
último `ContactAttempt` de la póliza ("Contactado", "No contestó", "Dejó mensaje",
"Interesado", "No interesado"), o "No contactado" si no hay intentos. Se visualiza
como badge de color en la columna "Última gestión" del dashboard.

## 6. Endpoints Expuestos

### Thymeleaf (HTML)

| Método | Ruta | Parámetros | Descripción |
|--------|------|-------------|-------------|
| GET | `/` | `?filter=all\|active\|expiring\|expired_lt_30\|expired_gt_30\|interested\|not_interested` | Dashboard |
| GET | `/policy/{id}` | — | Detalle de póliza (JSON) |
| GET | `/policy/nueva` | — | Formulario crear póliza |

### REST API (JSON)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/policies?advisorId=1&filter=all` | Listar pólizas |
| GET | `/api/policies/{id}` | Detalle de póliza |
| POST | `/api/policies` | Crear póliza (con cliente nuevo) |
| PUT | `/api/policies/{id}` | Editar campos de póliza |
| PUT | `/api/policies/{id}/renew` | Renovar póliza |
| POST | `/api/contact-attempts` | Registrar gestión |
| POST | `/api/import/clients` | Importar clientes y pólizas desde Excel o XML (multipart) |
| GET | `/api/clients?advisorId=1` | Listar clientes |
| GET | `/api/clients/{id}` | Detalle de cliente |
| PUT | `/api/clients/{id}` | Editar campos de cliente |
| GET | `/api/stats?advisorId=1` | Estadísticas del dashboard |
| GET | `/swagger-ui.html` | OpenAPI UI |

## 7. Trade-offs Considerados

1. **SQLite vs PostgreSQL/MySQL**: SQLite es el requisito de la prueba. En
   producción con multi-asesor y concurrencia real, migraríamos a PostgreSQL.

2. **JPA con SQLite vs JDBC puro**: JPA con el dialecto comunitario de Hibernate 6
   funciona pero tiene limitaciones (no soporta `FOR UPDATE`, constraints reales).
   Para un MVP es aceptable.

3. **Thymeleaf vs React/Vue**: Thymeleaf permite tener todo en un solo proyecto
   Spring Boot, sin build steps, sin CORS. React sería más apropiado para una app
   real con mejor UX, pero agrega complejidad innecesaria para esta prueba.

4. **Renovación como nueva póliza vs update in-place**: Crear una nueva póliza
   preserva el historial completo. Update in-place perdería la trazabilidad.

5. **Seed data con INSERT OR IGNORE**: Permite que la app arranque siempre con
   datos de demostración sin importar cuántas veces se reinicie.

6. **Auto-inyección `@Lazy self`**: Para que métodos `@Transactional` puedan
   llamar a otros métodos transaccionales del mismo respetando el proxy de Spring.
   Los tests usan `ReflectionTestUtils.setField()` para inyectar `self`.

7. **Mockito + ReflectionTestUtils**: Los tests unitarios evitan levantar el
   contexto de Spring para velocidad. `self` se inyecta manualmente con
   `ReflectionTestUtils` para que las auto-llamadas transaccionales funcionen.
