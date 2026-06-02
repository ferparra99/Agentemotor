# Agentemotor — Panel de Gestión para Asesores de Seguros

Aplicación que reemplaza el Excel de María con un sistema web para gestionar pólizas, registrar contactos con clientes y controlar renovaciones dentro de la ventana regulatoria de 30 días.

## Cómo correrlo

**Requisitos**: Java 17+ y Maven (incluido como `mvnw`)

```bash
# 1. Compilar y ejecutar tests
./mvnw clean test

# 2. Iniciar la aplicación
./mvnw spring-boot:run

# 3. Abrir en el navegador
# http://localhost:8080/
```

La base de datos SQLite (`agentemotor.db`) se crea automáticamente en el directorio del proyecto con datos de demostración precargados.

## Decisiones de Diseño

1. **Spring Boot 3 + JPA + SQLite**: Stack que permite desarrollo rápido con persistencia embebida. Se usó el dialecto comunitario de Hibernate 6 (`hibernate-community-dialects`) para compatibilidad con SQLite.

2. **Thymeleaf + Vanilla JS**: Una sola pantalla principal con server-side rendering para el HTML y JavaScript plano para las interacciones (modal de detalle, registro de gestiones, renovación). Sin dependencias frontend, sin build steps.

3. **Arquitectura en capas**: Controller → Service (interface + impl) → Repository (Spring Data JPA). La lógica de negocio está aislada en `PolicyServiceImpl`, incluyendo la clasificación de prioridad basada en la ventana de 30 días.

4. **Seed data idempotente**: `data.sql` usa `INSERT OR IGNORE` para que los datos de demostración se carguen una sola vez sin importar cuántas veces se reinicie la app.

5. **Ventana de 30 días como constante**: Centralizada en `PolicyServiceImpl.RENEWAL_WINDOW_DAYS = 30`. Fácil de modificar si cambia la regulación.

## Qué dejé fuera y por qué

Ver `spec.md` sección "No Construido" para la lista completa con justificaciones.

## Si esto fuera a producción mañana

1. **Autenticación y multi-asesor**: Cada asesor debe tener su propia cuenta y solo ver sus clientes.
2. **Migrar a PostgreSQL**: SQLite no escala con concurrencia real. Agentemotor procesa 1.8M cotizaciones/mes.
3. **Pool de conexiones con HikariCP** (ya incluido) y configurar límites apropiados.
4. **Notificaciones**: Recordatorios automáticos por email/WhatsApp cuando una póliza está por vencer.
5. **Logging estructurado**: Centralizar logs para debugging y monitoreo.
6. **Manejo de errores global**: Agregar `@ControllerAdvice` para errores HTTP consistentes.
7. **Rate limiting**: Proteger los endpoints REST de abuso.
8. **CORS y seguridad**: Configurar CORS apropiadamente y agregar HTTPS.
9. **Pruebas de integración con BD real**: Las pruebas actuales usan H2 en memoria. Para producción, deben correr contra SQLite.

## Tiempo aproximado

**~4 horas reales** distribuidas en:
- 45 min: Análisis y planificación
- 1.5 h: Implementación del backend (modelos, repositorios, servicio, controladores)
- 1 h: Frontend Thymeleaf + JS (dashboard + modal)
- 30 min: Tests y seed data
- 30 min: Documentación (spec, code_review, README)

## Video

[Link al video (Loom / YouTube unlisted)] — Pendiente de grabar.

## Mejora para esta prueba técnica

Una cosa que mejoraría: **proporcionar un set de datos de prueba estándar en el enunciado**. La prueba dice "datos pre-cargados incluidos si tu app lo necesita", pero no especifica un volumen ni escenarios mínimos. Tener un CSV o JSON de ejemplo con ~20 pólizas en distintos estados (vigentes, por vencer, vencidas <30d, vencidas >30d, renovadas) estandarizaría la evaluación y permitiría comparar apples-to-apples entre candidatos. Actualmente, cada evaluado decide qué datos incluir, lo que hace difícil comparar si la lógica de ventana de 30 días funciona correctamente.
