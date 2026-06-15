# 12_constantes_app.md — Centralización de constantes y renombre de PolicyConstants

## Prompt
Solicitud para extraer valores hardcodeados del código a archivos de constantes,
renombrar `PolicyConstants` por haber dividido el controller principal, y actualizar
la documentación del proyecto.

## Cambios realizados

### 1. Renombre de `PolicyConstants.java` → `AppConstants.java`
El nombre anterior reflejaba una arquitectura donde solo existía `PolicyRestController`.
Al dividir en 4 controladores por recurso, el archivo pasó a llamarse `AppConstants`
para indicar que contiene constantes de toda la aplicación.

### 2. Nuevas constantes agregadas

| Constante | Valor | Ubicaciones que reemplaza |
|-----------|-------|---------------------------|
| `DEFAULT_PHONE` | `""` | `PolicyServiceImpl:111`, `ClientServiceImpl:77`, `ImportServiceImpl:75` |
| `MEDIUM_PRIORITY_DAYS` | `30` | `PolicyServiceImpl:271` — threshold media/baja |
| `POLICY_TERM_MONTHS` | `12` | `PolicyServiceImpl:174` — meses al crear renovación |
| `RENEWAL_POLICY_SUFFIX` | `"-R"` | `PolicyServiceImpl:171` — sufijo al renovar |
| `FILTER_ALL`, `FILTER_ACTIVE`, `FILTER_EXPIRING`, `FILTER_EXPIRED_LT_30`, `FILTER_EXPIRED_GT_30`, `FILTER_INTERESTED`, `FILTER_NOT_INTERESTED` | `"all"`, etc. | `PolicyServiceImpl:63-86`, `PolicyController:23`, `DashboardController:24` |
| `CONTACT_RESULT_NO_CONTACT`, `CONTACT_RESULT_CONTACTED`, `CONTACT_RESULT_NO_ANSWER`, `CONTACT_RESULT_LEFT_MESSAGE`, `CONTACT_RESULT_INTERESTED`, `CONTACT_RESULT_NOT_INTERESTED` | `"No contactado"`, etc. | `PolicyServiceImpl:192-206` |
| `ERROR_IMPORT_FILE_READ` | `"Error al leer el archivo: "` | `ImportServiceImpl:111` |
| `ERROR_IMPORT_MISSING_FIELDS` | `"Campos requeridos faltantes: "` | `ImportServiceImpl:125` |
| `ERROR_IMPORT_INVALID_TYPE_PREFIX` | `"Tipo de póliza inválido: "` | `ImportServiceImpl:130` |
| `VIEW_DASHBOARD` | `"dashboard"` | `DashboardController:33` |
| `VIEW_POLICY_FORM` | `"policy-form"` | `DashboardController:44` |

### 3. Secciones con comentarios de uso
Cada grupo de constantes en `AppConstants.java` tiene un comentario separador
que indica en qué clases se utiliza (`// Usado en: ...`), facilitando la
navegación y el mantenimiento.

### 4. Archivos modificados (9 Java + 2 documentación)

- `utils/PolicyConstants.java` → eliminado (reemplazado por `AppConstants.java`)
- `utils/AppConstants.java` → creado con secciones comentadas
- `service/PolicyServiceImpl.java` — import + 9 reemplazos de hardcode
- `service/ClientServiceImpl.java` — import + `DEFAULT_PHONE`
- `service/ImportServiceImpl.java` — import + 4 reemplazos
- `service/ContactAttemptServiceImpl.java` — solo import
- `service/StatsServiceImpl.java` — import + `URGENT_THRESHOLD_DAYS`
- `controller/PolicyController.java` — import + `FILTER_ALL`
- `controller/DashboardController.java` — import + `FILTER_ALL` + `VIEW_*`
- `controller/ImportController.java` — solo import
- `README.md` — actualizada directriz de constantes y auto-inyección
- `ai_history/12_constantes_app.md` — esta entrada

## Pruebas
- 34 tests, todos pasan
- `mvnw clean test` → BUILD SUCCESS
