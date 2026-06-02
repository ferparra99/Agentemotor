# Agentemotor — Base de conocimiento completa

## Descripción

Aplicación web para **María**, asesora de seguros, que reemplaza su hoja de cálculo de Excel para la gestión de pólizas de clientes, intentos de contacto y renovaciones dentro de la **ventana regulatoria de 30 días** en Colombia.

---

## Stack tecnológico

| Componente | Versión | Propósito |
|-----------|---------|-----------|
| Java | 17 | Lenguaje |
| Spring Boot | 3.5.14 | Framework web |
| Spring Data JPA | — | ORM / persistencia |
| Hibernate | 6.6.49.Final (community-dialects) | ORM + dialecto SQLite |
| SQLite | 3.49.1 | Base de datos embebida (producción) |
| Thymeleaf | — | Server-side rendering (UI) |
| Lombok | — | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.8.16 | Documentación API (`/swagger-ui.html`) |
| H2 | — | Base de datos en memoria (tests) |
| JUnit 5 + Spring Boot Test | — | Tests |

---

## Estructura del proyecto

```
agentemotor/
├── pom.xml
├── spec.md
├── README.md
├── code_review.md
├── HELP.md
├── base.md
├── ai_history/
│   ├── 01_planeacion.md
│   ├── 02_implementacion.md
│   ├── 03_code_review.md
│   ├── 04_documentacion.md
│   ├── 05_fix_filtros_fechas.md
│   ├── 06_prioridades_espanol.md
│   └── 07_creacion_poliza.md
├── src/
│   ├── main/
│   │   ├── java/com/agentemotor/
│   │   │   ├── AgentemotorApplication.java
│   │   │   ├── model/
│   │   │   │   ├── Advisor.java
│   │   │   │   ├── Client.java
│   │   │   │   ├── Policy.java
│   │   │   │   ├── PolicyType.java          (AUTO, HOGAR, VIDA)
│   │   │   │   ├── PolicyStatus.java         (ACTIVE, EXPIRED, RENEWED)
│   │   │   │   ├── ContactAttempt.java
│   │   │   │   ├── ContactAttemptType.java   (CALL, EMAIL, WHATSAPP)
│   │   │   │   └── ContactAttemptResult.java (CONTACTED, NO_ANSWER, LEFT_MESSAGE, INTERESTED, NOT_INTERESTED)
│   │   │   ├── repository/
│   │   │   │   ├── AdvisorRepository.java
│   │   │   │   ├── ClientRepository.java
│   │   │   │   ├── PolicyRepository.java
│   │   │   │   └── ContactAttemptRepository.java
│   │   │   ├── service/
│   │   │   │   ├── PolicyService.java      (interface)
│   │   │   │   └── PolicyServiceImpl.java  (implementación)
│   │   │   ├── controller/
│   │   │   │   ├── PolicyRestController.java  (REST API)
│   │   │   │   └── DashboardController.java   (Thymeleaf)
│   │   │   └── dto/
│   │   │       ├── PolicySummaryDTO.java
│   │   │       ├── PolicyDetailDTO.java
│   │   │       ├── DashboardStatsDTO.java
│   │   │       ├── ContactAttemptDTO.java
│   │   │       ├── ContactAttemptRequestDTO.java
│   │   │       ├── RenewRequestDTO.java
│   │   │       ├── CreatePolicyRequestDTO.java
│   │   │       └── ClientDetailDTO.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── data.sql                     (seed data)
│   │       └── templates/
│   │           ├── dashboard.html           (panel principal)
│   │           └── policy-form.html         (formulario crear póliza)
│   └── test/
│       └── java/com/agentemotor/
│           ├── AgentemotorApplicationTests.java
│           ├── service/
│           │   └── PolicyServiceTest.java   (3 tests críticos)
│           └── controller/
│               └── PolicyRestControllerTest.java
```

---

## Modelo de datos

### Advisor (Asesor)
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | Long | PK, auto-generado |
| name | String | NOT NULL |
| email | String | NOT NULL |
| phone | String | — |

### Client (Cliente)
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | Long | PK, auto-generado |
| name | String | NOT NULL |
| phone | String | NOT NULL |
| email | String | — |
| notes | String (TEXT) | — |
| advisor | Advisor (ManyToOne) | NOT NULL |

### Policy (Póliza)
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | Long | PK, auto-generado |
| policyNumber | String | NOT NULL |
| type | PolicyType (enum) | AUTO / HOGAR / VIDA, NOT NULL |
| insurer | String | NOT NULL |
| startDate | LocalDate | NOT NULL |
| expirationDate | LocalDate | NOT NULL |
| status | PolicyStatus (enum) | ACTIVE / EXPIRED / RENEWED, NOT NULL, default ACTIVE |
| renewalCount | Integer | NOT NULL, default 0 |
| client | Client (ManyToOne) | NOT NULL, LAZY |
| advisor | Advisor (ManyToOne) | NOT NULL, LAZY |

### ContactAttempt (Intento de contacto / Gestión)
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id | Long | PK, auto-generado |
| date | LocalDateTime | NOT NULL |
| type | ContactAttemptType (enum) | CALL / EMAIL / WHATSAPP, NOT NULL |
| result | ContactAttemptResult (enum) | CONTACTED / NO_ANSWER / LEFT_MESSAGE / INTERESTED / NOT_INTERESTED, NOT NULL |
| notes | String (TEXT) | — |
| policy | Policy (ManyToOne) | NOT NULL, LAZY |

### Enumeraciones

```
PolicyType:      AUTO | HOGAR | VIDA
PolicyStatus:    ACTIVE | EXPIRED | RENEWED
ContactAttemptType:   CALL | EMAIL | WHATSAPP
ContactAttemptResult: CONTACTED | NO_ANSWER | LEFT_MESSAGE | INTERESTED | NOT_INTERESTED
```

---

## Lógica de negocio

### Ventana de 30 días regulatoria

La normativa colombiana establece que los aseguradores deben contactar al cliente dentro de los 30 días posteriores al vencimiento de la póliza para gestión de renovación. La aplicación clasifica cada póliza según su urgencia:

| Prioridad (español) | Etiqueta interna | Condición |
|---------------------|-----------------|-----------|
| perdido | `perdido` | Vencido hace > 30 días (fuera de ventana) |
| urgente | `urgente` | Vencido hace > 7 días (dentro de ventana, crítica) |
| alta | `alta` | Vencido hace > 0 días (recién vencido) O vence en ≤ 7 días |
| media | `media` | Vence en ≤ 30 días |
| baja | `baja` | Vigente, vence en > 30 días |
| completada | `completada` | Póliza no activa (EXPIRED o RENEWED) |

### Filtros del dashboard

| Filtro (URL) | Descripción | Cómo funciona |
|-------------|-------------|---------------|
| `all` | Todas las pólizas del asesor | `findByAdvisorId()` |
| `active` | Pólizas vigentes | `findByAdvisorIdAndStatus(ACTIVE)` |
| `expiring` | Vencen en ≤ 30 días | `findByAdvisorIdAndStatus(ACTIVE)` + filtro `isBefore(today+30)` |
| `expired_lt_30` | Vencidas hace < 30 días | `findByAdvisorIdAndStatus(ACTIVE)` + filtro `isBefore(today) && !isBefore(today-30)` |
| `expired_gt_30` | Vencidas hace > 30 días (perdidas) | `findByAdvisorIdAndStatus(ACTIVE)` + filtro `isBefore(today-30)` |

### Renovación de póliza
1. La póliza original se marca como `RENEWED`
2. Se crea una **nueva** póliza con:
   - `policyNumber = original + "-R" + (renewalCount + 1)`
   - `startDate = newExpirationDate - 12 months`
   - `expirationDate = newExpirationDate` (provisto por el usuario)
   - `status = ACTIVE`, `renewalCount = original.renewalCount + 1`
   - Mismos `client` y `advisor`

### Creación de póliza
1. Valida que el cliente y el asesor existan
2. Crea póliza con:
   - `status = ACTIVE`, `renewalCount = 0`
   - Datos provistos: `policyNumber`, `type`, `insurer`, `startDate`, `expirationDate`, `clientId`

---

## API REST

| Método | Ruta | Request | Response | Descripción |
|--------|------|---------|----------|-------------|
| GET | `/api/policies?advisorId=1&filter=all` | query params | `List<PolicySummaryDTO>` | Listar pólizas con filtro |
| GET | `/api/policies/{id}` | path param | `PolicyDetailDTO` | Detalle de póliza |
| POST | `/api/policies` | `CreatePolicyRequestDTO` | `PolicySummaryDTO` (201) | Crear nueva póliza |
| PUT | `/api/policies/{id}/renew` | `RenewRequestDTO` | `PolicySummaryDTO` | Renovar póliza |
| POST | `/api/contact-attempts` | `ContactAttemptRequestDTO` | `ContactAttemptDTO` | Registrar gestión |
| GET | `/api/clients?advisorId=1` | query param | `List<ClientDetailDTO>` | Listar clientes |
| GET | `/api/clients/{id}` | path param | `ClientDetailDTO` | Detalle de cliente |
| GET | `/api/stats?advisorId=1` | query param | `DashboardStatsDTO` | Estadísticas del dashboard |

### DTOs de request

```json
// CreatePolicyRequestDTO
{
  "policyNumber": "AUTO-010-2026",
  "type": "AUTO",
  "insurer": "Seguros Sura",
  "startDate": "2026-01-15",
  "expirationDate": "2027-01-15",
  "clientId": 1
}

// RenewRequestDTO
{ "newExpirationDate": "2027-06-01" }

// ContactAttemptRequestDTO
{
  "policyId": 1,
  "type": "CALL",
  "result": "INTERESTED",
  "notes": "Cliente interesado en renovar"
}
```

### DTOs de response

```json
// PolicySummaryDTO
{
  "id": 1,
  "policyNumber": "AUTO-001-2025",
  "type": "AUTO",
  "insurer": "Seguros Sura",
  "clientName": "Carlos Méndez",
  "clientPhone": "+57 310 111 2233",
  "expirationDate": "2026-06-05",
  "daysUntilExpiry": 3,
  "daysOverdue": 0,
  "status": "ACTIVE",
  "priority": "alta",
  "contactAttempts": 0,
  "recommendedAction": "Gestionar renovación antes del vencimiento."
}

// DashboardStatsDTO
{
  "totalActive": 7,
  "expiringThisWeek": 1,
  "expiringThisMonth": 2,
  "expiredWithin30Days": 2,
  "expiredBeyond30Days": 1,
  "totalRenewed": 1
}

// PolicyDetailDTO (contiene todo lo de Summary + clientEmail + lista de contactAttempts)
```

---

## Rutas Thymeleaf

| Ruta | Método | Controller | Template | Descripción |
|------|--------|-----------|----------|-------------|
| `/` | GET | DashboardController | `dashboard.html` | Panel principal con stats, filtros, tabla, modal |
| `/policy/{id}` | GET | DashboardController | JSON (ResponseBody) | Detalle de póliza para el modal |
| `/policy/nueva` | GET | DashboardController | `policy-form.html` | Formulario para crear póliza |

---

## Configuración (`application.properties`)

```properties
spring.application.name=agentemotor

# SQLite Datasource
spring.datasource.url=jdbc:sqlite:agentemotor.db?date_string_format=yyyy-MM-dd
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.username=
spring.datasource.password=

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Seed data
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

spring.jpa.open-in-view=false

# Thymeleaf
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
spring.thymeleaf.cache=false
```

---

## Seed data (`data.sql`)

8 clientes con distintos perfiles + 8 pólizas en varios estados:

| Póliza | Cliente | Tipo | Vencimiento | Estado |
|--------|---------|------|-------------|--------|
| AUTO-001-2025 | Carlos Méndez | AUTO | now + 3 días | ACTIVE |
| HOGAR-002-2025 | Ana Lucía Rojas | HOGAR | now + 20 días | ACTIVE |
| AUTO-003-2025 | Pedro Infante | AUTO | now + 120 días | ACTIVE |
| VIDA-004-2025 | Laura Jiménez | VIDA | now + 180 días | ACTIVE |
| AUTO-005-2024 | Jorge Velásquez | AUTO | now - 2 días | ACTIVE |
| AUTO-006-2024 | Diana Moreno | AUTO | now - 15 días | ACTIVE |
| AUTO-007-2024 | Ricardo Bustos | AUTO | now - 45 días | ACTIVE |
| AUTO-008-2023 | Laura Jiménez | AUTO | 2024-06-01 | **RENEWED** (renovada) |

Usa `INSERT OR IGNORE` para idempotencia. Las fechas con `DATE('now', '+/- N days')` se calculan al insertar.

---

## Tests

### PolicyServiceTest (3 tests críticos)
1. **VentanaDe30Dias_ClasificaPrioridades** — Verifica que pólizas en distintos estados reciben la prioridad correcta
2. **Renovacion_Poliza_CreaNuevaYActualizaOriginal** — Verifica el flujo completo de renovación
3. **RegistroGestion_ContactAttempt_SeGuardaYRetorna** — Verifica que un intento de contacto se persiste correctamente

### PolicyRestControllerTest
- Test de integración para `GET /api/policies`, `GET /api/policies/{id}`, `POST /api/contact-attempts`, `PUT /api/policies/{id}/renew`

### Configuración de tests
- Perfil "test" que reemplaza SQLite por H2 en memoria
- `schema.sql` con las sentencias CREATE TABLE
- `data.sql` con datos de prueba

---

## Decisiones clave y trade-offs

| Decisión | Alternativa descartada | Razón |
|----------|----------------------|-------|
| **Thymeleaf** | React | Sin build steps, sin CORS, un solo proyecto, suficiente para MVP de 1 pantalla |
| **SQLite** | PostgreSQL / MySQL | Embebido, sin servidor, ideal para POC |
| **Filtrado en memoria** | JPQL con fechas | SQLite almacena DATE como TEXT → JPQL `BETWEEN` falla. Usar `LocalDate.isBefore()` en memoria es confiable |
| **Renovación = nueva póliza** | Actualizar in-place | Preserva historial completo |
| **Sin autenticación** | Spring Security | POC de un solo asesor |
| **Prioridades en español** | Inglés | UI para María, mejor comprensión |
| **Lombok** | Getters/setters manuales | Reduce ~70% de boilerplate |
| **DTOs separados** | Entidades expuestas | Evita LazyInitializationException, separa modelo de presentación |
| **`open-in-view=false`** | true (default) | Evita LazyInitializationException silenciosas en la vista |

---

## Bugs corregidos (historial)

### Bug 1: Filtros de fecha no funcionan (05_fix_filtros_fechas.md)
- **Síntoma**: Los filtros `expiring`, `expired_lt_30`, `expired_gt_30` devolvían 0 resultados o datos incorrectos
- **Causa raíz**: SQLite almacena columnas DATE como TEXT. JPQL `p.expirationDate BETWEEN :today AND :thirtyDays` compara strings, no fechas
- **Solución**: Eliminar `columnDefinition = "TEXT"` de campos LocalDate, agregar `?date_string_format=yyyy-MM-dd` al JDBC URL, y usar filtrado en memoria con streams Java (`LocalDate.isBefore()/isAfter()`)

### Bug 2: Thymeleaf no funciona (404)
- **Síntoma**: `http://localhost:8080/` devolvía 404
- **Causa raíz**: `spring-boot-starter-thymeleaf` no estaba en `pom.xml`
- **Solución**: Agregar dependencia

### Bug 3: Prioridades en inglés
- **Síntoma**: Dashboard mostraba `high`, `low`, etc. en inglés
- **Solución**: Cambiar etiquetas a español: alta, media, baja, urgente, perdido, completada

---

## Cómo ejecutar

```bash
# Compilar y correr tests
./mvnw clean test

# Iniciar la aplicación
./mvnw spring-boot:run

# Abrir en el navegador
http://localhost:8080/

# Documentación OpenAPI
http://localhost:8080/swagger-ui.html
```

> **Importante**: Borrar `agentemotor.db` antes del primer arranque después de cambios en el esquema. El seed data usa `INSERT OR IGNORE` que evita duplicados pero no actualiza registros existentes.

---

## Archivos entregables

| Archivo | Descripción |
|---------|-------------|
| `spec.md` | Análisis completo del problema, decisiones, supuestos, flujos, trade-offs |
| `README.md` | Instrucciones de uso, decisiones de diseño, qué falta para producción |
| `code_review.md` | Revisión de 4 problemas reales en el snippet Flask de la prueba |
| `ai_history/*.md` | 7 archivos con el registro completo del desarrollo con IA |

---

## API Stats (ejemplo real, 2 jun 2026)

```
GET /api/stats?advisorId=1
{
  "totalActive": 7,
  "expiringThisWeek": 1,
  "expiringThisMonth": 2,
  "expiredWithin30Days": 2,
  "expiredBeyond30Days": 1,
  "totalRenewed": 1
}
```

---

## Dependencias Maven (`pom.xml`)

```xml
<parent>org.springframework.boot:spring-boot-starter-parent:3.5.14</parent>
<properties><java.version>17</java.version></properties>

<dependencies>
  spring-boot-starter-data-jpa
  spring-boot-starter-web
  spring-boot-starter-thymeleaf
  springdoc-openapi-starter-webmvc-ui:2.8.16
  spring-boot-devtools (runtime)
  sqlite-jdbc (runtime)
  hibernate-community-dialects
  lombok (optional)
  spring-boot-starter-test (test)
  spring-restdocs-mockmvc (test)
  h2 (test)
</dependencies>
```

---

## Licencia

Proyecto de evaluación técnica — Agentemotor.
