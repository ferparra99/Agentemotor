# 01_planeacion.md — Análisis y planificación

## Prompt inicial
Solicitud de evaluar la prueba técnica de Agentemotor y generar un plan para afrontarla.

## Discusión con la IA
- Se analizó el enunciado completo de la prueba técnica
- Se revisó el proyecto existente: Spring Boot 3.5.14 + Java 17 + JPA + SQLite + Lombok + OpenAPI
- Se discutieron opciones de frontend (React vs Thymeleaf vs Vanilla HTML)
- Se decidió Thymeleaf por simplicidad y cohesión con Spring Boot
- Se discutió el stack de tests (Spring Boot Test + JUnit 5)
- Se acordó incluir seed data con data.sql

## Decisiones clave
- Frontend: Thymeleaf (server-side rendering)
- Tests: Spring Boot Test + JUnit 5 con H2 in-memory
- Seed data: Sí, con INSERT OR IGNORE para idempotencia
- Modelo de datos: Advisor, Client, Policy, ContactAttempt
- No construir: autenticación, multi-asesor, notificaciones
