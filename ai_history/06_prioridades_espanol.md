# 06_prioridades_espanol.md — Prioridades en español

## Prompt
Cambiar las prioridades en la UI a español para mejor comprensión de María.

## Cambio realizado
Se modificó el método `getPriorityLabel()` en `PolicyServiceImpl.java` para retornar etiquetas en español:

| Prioridad | Valor original | Nuevo valor |
|-----------|---------------|-------------|
| Perdida   | `lost`        | `perdido`   |
| Urgente   | `urgent`      | `urgente`   |
| Alta      | `high`        | `alta`      |
| Media     | `medium`      | `media`     |
| Baja      | `low`         | `baja`      |
| Completada| `completed`   | `completada`|

## Impacto
- La UI muestra prioridades en español en la tabla y en las tarjetas de estadísticas
- No afecta la lógica interna de negocio (solo cambia la etiqueta de presentación)
- Se usa uniformemente en `dashboard.html`
