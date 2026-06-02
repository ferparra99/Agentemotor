# 05_fix_filtros_fechas.md — Corrección de filtros por fecha

## Prompt
Solucionar error: los filtros "expiring", "expired_lt_30" y "expired_gt_30" no devuelven resultados correctos.

## Diagnóstico
- SQLite almacena `DATE` como `TEXT` en formato `yyyy-MM-dd`
- JPQL `BETWEEN` y `<` usan comparación léxica de strings, no de fechas
- La query JPQL `WHERE p.status = 'ACTIVE' AND p.expirationDate BETWEEN :today AND :thirtyDays` NO funciona porque SQLite compara strings
- Se investigaron dos alternativas:
  1. `columnDefinition = "DATE"` + JDBC url con `?date_string_format=yyyy-MM-dd` — esto logra que Hibernate lea/escriba correctamente las fechas, PERO sigue sin funcionar en filtros JPQL porque SQLite internamente sigue almacenando como TEXT
  2. Filtrado en memoria con Java `LocalDate.isBefore()` / `LocalDate.isAfter()` después de traer todos los registros

## Solución aplicada
- Se quitó `columnDefinition = "TEXT"` de los campos `LocalDate` en `Policy.java`
- Se agregó `?date_string_format=yyyy-MM-dd` a la URL de JDBC en `application.properties`
- Se refactorizó `PolicyServiceImpl` para usar **filtrado en memoria** con streams Java:
  - Se traen todas las pólizas del asesor (con query JPQL solo por advisorId + status)
  - Se filtran con `LocalDate.isBefore()` / `isAfter()` / `isEqual()`
- Esto eliminó la dependencia de JPQL para comparaciones de fechas

## Resultado
- Los 5 filtros (`all`, `active`, `expiring`, `expired_lt_30`, `expired_gt_30`) ahora devuelven resultados correctos
- Tests pasan con H2 (que sí soporta comparación de fechas en SQL)
- Trade-off: mayor volumen de datos en memoria, pero aceptable para ~280 pólizas
