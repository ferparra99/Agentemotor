# Agentemotor — Panel de Gestión para Asesores de Seguros

Aplicación web que reemplaza el Excel de María con un sistema para gestionar pólizas,
registrar contactos con clientes y controlar renovaciones dentro de la ventana
regulatoria de 30 días en Colombia.

## Cómo ejecutar

**Requisitos**: Java 17+ y Maven (incluido como `mvnw`)

```bash
# 1. Compilar y ejecutar tests
./mvnw clean test

# 2. Iniciar la aplicación
./mvnw spring-boot:run

# 3. Abrir en el navegador
# http://localhost:8080/
```

Tambien puede ser ejecutado en la siguiente URL: https://agentemotor.onrender.com el cual es un servidor en la nube que toma los cambios del github https://github.com/ferparra99/Agentemotor

La base de datos SQLite (`agentemotor.db`) se crea automáticamente en el directorio
del proyecto con datos de demostración precargados.

> Si modificas el esquema (nuevas columnas, cambios en enums), borra `agentemotor.db`
> antes de reiniciar para que se regenere desde `data.sql`.

## Funcionalidades

| Funcionalidad | Cómo |
|---------------|------|
| Dashboard con resumen y tabla de pólizas | Pantalla principal con tarjetas de estadísticas y tabla filtrable |
| Filtros por estado | Todas, Vigentes, Por vencer (30d), Vencidas <30d, Perdidas, **Interesados**, **No interesados** |
| Prioridades en español | perdido (PERDIDO), urgente (VENCIDO), alta/media/baja (ACTIVO), completada (RENOVADA) |
| Última gestión por póliza | Columna separada con badge de color: Contactado (verde), No contestó (amarillo), Dejó mensaje (azul), Interesado (naranja), No interesado (gris), No contactado (gris claro) |
| Creación rápida de póliza | Formulario donde se escribe nombre/teléfono del cliente y se crea automáticamente |
| Detalle y gestiones | Modal con historial de contactos, registro de nuevo intento |
| Edición inline | Botón "Editar" en el modal que permite modificar cliente y póliza |
| Renovación | Crea nueva póliza extendida y marca la original como renovada |
| Auto-refresh | La tabla se actualiza automáticamente al cerrar el modal |

## Stack tecnológico

| Componente | Versión | Propósito |
|-----------|---------|-----------|
| Java | 17 | Lenguaje |
| Spring Boot | 3.5.14 | Framework web |
| Spring Data JPA | — | ORM / persistencia |
| Hibernate | 6.6.49.Final (community-dialects) | ORM + dialecto SQLite |
| SQLite | 3.49.1 | Base de datos embebida |
| Thymeleaf | — | Server-side rendering |
| Lombok | — | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.8.16 | Documentación API (`/swagger-ui.html`) |
| H2 | — | Base de datos en memoria (tests) |
| JUnit 5 + Mockito | — | Tests unitarios y de integración |

## Estructura del proyecto

```
agentemotor/
├── pom.xml
├── spec.md                    # Especificación funcional
├── README.md
├── base.md                    # Base de conocimiento
├── code_review.md
├── ai_history/                # Trazabilidad del desarrollo
│   └── 01_... a 08_...
├── src/main/java/com/agentemotor/
│   ├── model/                 # Entidades JPA + Enums
│   ├── repository/            # Spring Data Repositories
│   ├── service/               # Lógica de negocio (interface + impl)
│   ├── controller/            # REST API + Thymeleaf
│   ├── dto/                   # Objetos de transferencia
│   └── utils/                 # Constantes centralizadas
├── src/main/resources/
│   ├── application.properties
│   ├── data.sql               # Seed data idempotente
│   └── templates/             # Thymeleaf (dashboard, policy-form)
├── src/test/java/
│   ├── AgentemotorApplicationTests.java
│   └── service/
│       ├── PolicyServiceTest.java        # 3 tests de integración
│       └── PolicyServiceImplTest.java     # 17 tests unitarios (Mockito)
└── agentemotor.db             # SQLite (se genera automáticamente)
```

## Decisiones de diseño

1. **Spring Boot 3 + JPA + SQLite**: Desarrollo rápido con persistencia embebida.
   Dialecto comunitario de Hibernate 6 para compatibilidad con SQLite.

2. **Thymeleaf + Vanilla JS**: Una sola pantalla con server-side rendering y
   JavaScript plano para el modal. Sin dependencias frontend ni build steps.

3. **Arquitectura en capas**: Controller → Service (interface + impl) → Repository.
   Lógica de negocio aislada en `PolicyServiceImpl`.

4. **Seed data idempotente**: `data.sql` usa `INSERT OR IGNORE` para carga única.

5. **Constantes centralizadas**: `utils/PolicyConstants.java` agrupa IDs, días de
   ventana, mensajes de error, prioridades y textos de acción. Fácil de modificar
   si cambia la regulación.

6. **Auto-inyección `@Lazy self`**: Para que llamadas internas entre métodos
   `@Transactional` pasen por el proxy de Spring y respeten los límites de
   transacción.

7. **Creación de cliente al vuelo**: Al crear una póliza se escribe nombre y
   teléfono; el sistema crea el `Client` automáticamente. Sin paso previo de
   registro de cliente.

8. **Estados en español**: `PolicyStatus` usa `ACTIVO`, `VENCIDO`, `PERDIDO`, `RENOVADA`.
   `updatePolicyStatuses()` clasifica automáticamente según la ventana de 30 días.

9. **Última gestión como columna independiente**: La tabla del dashboard separa el
   conteo de gestiones del resultado de la última gestión (badge con color). El
   badge de interés (Interesado/No interesado) se eliminó de la columna prioridad
   y ahora se muestra en la columna "Última gestión" junto con los demás resultados.

## API REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/policies?advisorId=1&filter=all` | Listar pólizas |
| GET | `/api/policies/{id}` | Detalle de póliza |
| POST | `/api/policies` | Crear póliza (con cliente nuevo) |
| PUT | `/api/policies/{id}` | Editar campos de póliza |
| PUT | `/api/policies/{id}/renew` | Renovar póliza |
| POST | `/api/contact-attempts` | Registrar gestión |
| GET | `/api/clients?advisorId=1` | Listar clientes |
| GET | `/api/clients/{id}` | Detalle de cliente |
| PUT | `/api/clients/{id}` | Editar campos de cliente |
| GET | `/api/stats?advisorId=1` | Estadísticas del dashboard |

Documentación interactiva en `/swagger-ui.html`.

## Tests

**21 tests en total — todos pasan:**

- `AgentemotorApplicationTests` (1): contexto de Spring
- `PolicyServiceTest` (3): ventana 30 días, renovación, registro de gestión
- `PolicyServiceImplTest` (17):
  - `createPolicy`: 4 tests (éxito, sin teléfono, advisor no encontrado, tipo inválido)
  - `updatePolicy`: 4 tests (completo, parcial, no encontrado, tipo inválido)
  - `updateClient`: 3 tests (completo, parcial, no encontrado)
  - `Filter interested/not_interested`: 6 tests (filtro, sin intentos, múltiples, vencida excluida)

```bash
./mvnw clean test
```

## Troubleshooting

### `No enum constant PolicyStatus.RENEWED`

Ocurre cuando cambias los valores del enum `PolicyStatus` (ej: `ACTIVE` → `ACTIVA`)
pero la base de datos SQLite aún tiene los valores viejos.

**Solución:** Borra `agentemotor.db` y reinicia la aplicación.

### La tabla no se actualiza después de una acción

El modal ejecuta `refreshTable()` al cerrarse. Si aún así no ves cambios,
recarga la página manualmente o haz clic en el filtro actual.

## Lo que falta para producción

1. **Autenticación y multi-asesor** — Cada asesor con su cuenta.
2. **Migrar a PostgreSQL** — SQLite no escala con concurrencia real.
3. **Notificaciones** — Recordatorios automáticos por email/WhatsApp.
4. **Manejo de errores global** — `@ControllerAdvice` para respuestas HTTP consistentes.
5. **Logging estructurado** — Para debugging y monitoreo.
6. **CORS y HTTPS** — Seguridad en producción.
