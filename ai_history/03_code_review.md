# 03_code_review.md — Revisión del código Flask

## Prompt
Evaluar el código Python/Flask proporcionado en la prueba técnica.

## Análisis con la IA
Se identificaron 4 problemas reales de producción:

1. **Conexiones no cerradas** (DB leak) → El mayor riesgo para producción
2. **Ventana de 30 días ignorada** → Lógica de negocio incorrecta
3. **Sin manejo de errores** → 500 silenciosos y datos corruptos
4. **N+1 queries** → 201 queries por request con 100 pólizas

Se documentó:
- Línea exacta del problema
- Impacto en producción
- Impacto en el negocio de María y Agentemotor
- Código de ejemplo para la corrección
