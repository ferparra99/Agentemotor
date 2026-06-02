# Spec — Agentemotor: Panel de Gestión para Asesores de Seguros

## 1. Entendimiento del Problema

María, una asesora de seguros con 280 clientes activos, usa un Excel para gestionar las pólizas de su cartera. Cada lunes filtra las pólizas que vencen ese mes, llama cliente por cliente, marca una columna "gestionado" con una X, y cuando renuevan actualiza la fecha. Su flujo tiene problemas concretos:

- **El Excel se daña, se duplica, se pierde contexto** — no hay un registro histórico confiable.
- **Pérdida de clientes por vencimientos no detectados** — 5 a 10 clientes al mes se van con otro asesor porque María no identificó a tiempo que una póliza había vencido.
- **Sin trazabilidad** — no queda registro de qué se le ofreció a cada cliente ni cuándo se contactó.

**Contexto regulatorio crítico**: En Colombia, las pólizas de auto tienen una ventana de 30 días post-vencimiento para renovar sin perder el historial. Pasados los 30 días, la renovación se trata como nueva contratación y el asesor compite con cualquier otro intermediario.

## 2. Decisiones de Construcción

### Construido

| Funcionalidad | Justificación |
|---|---|
| Dashboard con resumen de pólizas por estado | Reemplaza la vista principal del Excel de María |
| Filtros: todas, vigentes, por vencer, vencidas <30d, vencidas >30d | Refleja la ventana regulatoria de 30 días |
| Vista detalle de póliza con historial de gestiones | Reemplaza la columna "gestionado" del Excel |
| Registro de intentos de contacto (tipo, resultado, notas) | Da trazabilidad a las acciones de María |
| Acción de renovar póliza con nueva fecha | Reemplaza la actualización manual en el Excel |
| Priorización automática (low/medium/high/urgent/lost) | Ayuda a María a enfocarse en lo crítico |
| Seed data precargada con escenarios reales | La app funciona desde el primer arranque |
| API REST completa | Permite integración futura |
| Swagger/OpenAPI disponible en /swagger-ui.html | Documentación interactiva de la API |
| 3 tests del caso más crítico (ventana 30 días, renovación, registro de gestión) | Validación del core business |

### No Construido (y por qué)

| Funcionalidad | Razón |
|---|---|
| Autenticación de usuarios | Prueba de concepto con un solo asesor. Aporta fricción sin valor para el alcance |
| Multi-asesor | María es la única usuaria. El ID del asesor está hardcodeado |
| Notificaciones automáticas (email, WhatsApp) | Excede el tiempo estimado. Se reemplaza con la priorización visual |
| Reportes y estadísticas avanzadas | María necesita gestionar, no analizar. El dashboard básico es suficiente |
| Roles y permisos | No hay multi-usuario |
| Carga masiva desde Excel | María migraría sus datos una vez. Se puede agregar después |
| Historial de pólizas anteriores al sistema | Se empieza desde cero con los datos de María |

## 3. Supuestos

1. **María es la asesora por defecto** — ID = 1, sin login. En producción se agregaría autenticación.
2. **Una renovación crea una nueva póliza** — La original pasa a estado RENEWED y se crea una nueva ACTIVE con la fecha extendida, manteniendo el historial completo.
3. **La ventana de 30 días aplica a todos los tipos de póliza** aunque el contexto regulatorio menciona específicamente autos. El sistema es genérico.
4. **Las aseguradoras son texto libre** — no hay tabla maestra de aseguradoras. Suficiente para el MVP.
5. **Los contactos se registran con timestamp automático** — la asesora no necesita ingresar la fecha manualmente.
6. **La base de datos SQLite se crea en el directorio de trabajo** — `agentemotor.db` aparece junto al JAR.

## 4. Flujos Principales

### Flujo 1: Dashboard diario
1. María abre `http://localhost:8080/`
2. Ve las tarjetas de resumen: activas, vencen esta semana, vencidas <30d, vencidas >30d, renovadas
3. La tabla muestra todas las pólizas con prioridad, cliente, vencimiento
4. Puede filtrar por estado usando los botones de filtro

### Flujo 2: Gestión de una póliza
1. María da clic en "Gestionar" en cualquier póliza
2. Se abre un modal con detalle completo: cliente, póliza, historial de gestiones
3. Puede registrar un nuevo intento de contacto (tipo, resultado, notas)
4. El historial se actualiza en tiempo real

### Flujo 3: Renovación
1. En el modal de detalle, María da clic en "Renovar póliza"
2. Ingresa la nueva fecha de vencimiento
3. El sistema marca la póliza original como RENEWED y crea una nueva ACTIVE
4. El dashboard se actualiza

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
  status: Enum [ACTIVE, EXPIRED, RENEWED]
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

## 6. Endpoints Expuestos

| Método | Ruta | Parámetros | Respuesta |
|---|---|---|---|
| GET | `/` | `?filter=all|active|expiring|expired_lt_30|expired_gt_30` | HTML (Thymeleaf) |
| GET | `/policy/{id}` | — | JSON (detalle póliza) |
| GET | `/api/policies` | `advisorId`, `filter` | JSON Lista |
| GET | `/api/policies/{id}` | — | JSON Detalle |
| PUT | `/api/policies/{id}/renew` | `{"newExpirationDate":"..."}` | JSON Póliza renovada |
| POST | `/api/contact-attempts` | `{"policyId","type","result","notes"}` | JSON |
| GET | `/api/clients/{id}` | — | JSON |
| GET | `/api/stats` | `advisorId` | JSON Dashboard |
| GET | `/swagger-ui.html` | — | OpenAPI UI |

## 7. Trade-offs Considerados

1. **SQLite vs PostgreSQL/MySQL**: SQLite es el requisito de la prueba. En producción con multi-asesor y concurrencia real, migraríamos a PostgreSQL.

2. **JPA con SQLite vs JDBC puro**: JPA con el dialecto comunitario de Hibernate 6 funciona pero tiene limitaciones (no soporta `FOR UPDATE`, constraints reales). Para un MVP es aceptable.

3. **Thymeleaf vs React/Vue**: Thymeleaf permite tener todo en un solo proyecto Spring Boot, sin build steps, sin CORS. React sería más apropiado para una app real con mejor UX, pero agrega complejidad innecesaria para esta prueba.

4. **Renovación como nueva póliza vs update in-place**: Crear una nueva póliza preserva el historial completo. Update in-place perdería la trazabilidad.

5. **Seed data con INSERT OR IGNORE**: Permite que la app arranque siempre con datos de demostración sin importar cuántas veces se reinicie.
