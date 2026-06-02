# 02_implementacion.md — Implementación del código

## Prompt
Ejecutar el plan completo implementando todo el código.

## Desarrollo con la IA
- Se configuró SQLite + JPA con `hibernate-community-dialects`
- Se eliminó `spring-boot-starter-data-jdbc` que causaba conflicto con SQLite
- Se crearon 4 entidades JPA: Advisor, Client, Policy, ContactAttempt
- Se implementó PolicyService con interface + implementación (principio SOLID)
- Lógica de ventana de 30 días implementada en `calculatePriority()`:
  - `lost`: días vencidos > 30
  - `urgent`: días vencidos > 7
  - `high`: días vencidos > 0 o vence en ≤ 7 días
  - `medium`: vence en ≤ 30 días
  - `low`: vigente
- Se crearon 2 controladores REST + 1 Thymeleaf controller
- Template dashboard.html con estadísticas, tabla filtrable, modal de detalle
- Seed data con 8 clientes, 8 pólizas en distintos estados, 4 intentos de contacto
- Tests: 3 casos críticos (ventana 30 días, renovación, registro de gestión)

## Hallazgos durante implementación
- SQLiteDialect de hibernate-community-dialects funciona correctamente
- INSERT OR IGNORE necesario para idempotencia del seed data
- Se eliminó spring-boot-starter-data-jdbc porque JDBC no soporta SQLite
