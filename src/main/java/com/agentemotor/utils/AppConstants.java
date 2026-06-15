package com.agentemotor.utils;

public final class AppConstants {

    private AppConstants() {}

    // ──────────────────────────────────────────────
    //  Configuración general
    //  Usado en: PolicyServiceImpl, StatsServiceImpl,
    //            DashboardController, ImportController
    // ──────────────────────────────────────────────
    public static final Long DEFAULT_ADVISOR_ID = 1L;
    public static final String DEFAULT_PHONE = "";
    public static final int RENEWAL_WINDOW_DAYS = 30;
    public static final int URGENT_THRESHOLD_DAYS = 7;
    public static final int MEDIUM_PRIORITY_DAYS = 30;
    public static final int POLICY_TERM_MONTHS = 12;

    // ──────────────────────────────────────────────
    //  Nombres de filtros (endpoints /api/policies/list)
    //  Usado en: PolicyServiceImpl, PolicyController,
    //            DashboardController
    // ──────────────────────────────────────────────
    public static final String FILTER_ALL = "all";
    public static final String FILTER_ACTIVE = "active";
    public static final String FILTER_EXPIRING = "expiring";
    public static final String FILTER_EXPIRED_LT_30 = "expired_lt_30";
    public static final String FILTER_EXPIRED_GT_30 = "expired_gt_30";
    public static final String FILTER_INTERESTED = "interested";
    public static final String FILTER_NOT_INTERESTED = "not_interested";

    // ──────────────────────────────────────────────
    //  Resultados de contacto (display en tabla)
    //  Usado en: PolicyServiceImpl.toSummaryDTO()
    // ──────────────────────────────────────────────
    public static final String CONTACT_RESULT_NO_CONTACT = "No contactado";
    public static final String CONTACT_RESULT_CONTACTED = "Contactado";
    public static final String CONTACT_RESULT_NO_ANSWER = "No contestó";
    public static final String CONTACT_RESULT_LEFT_MESSAGE = "Dejó mensaje";
    public static final String CONTACT_RESULT_INTERESTED = "Interesado";
    public static final String CONTACT_RESULT_NOT_INTERESTED = "No interesado";

    // ──────────────────────────────────────────────
    //  Prioridades (display en tabla)
    //  Usado en: PolicyServiceImpl.calculatePriority()
    // ──────────────────────────────────────────────
    public static final String PRIORITY_COMPLETADA = "completada";
    public static final String PRIORITY_PERDIDO = "perdido";
    public static final String PRIORITY_URGENTE = "urgente";
    public static final String PRIORITY_ALTA = "alta";
    public static final String PRIORITY_MEDIA = "media";
    public static final String PRIORITY_BAJA = "baja";

    // ──────────────────────────────────────────────
    //  Acciones recomendadas (display en detalle)
    //  Usado en: PolicyServiceImpl.calculateRecommendedAction()
    // ──────────────────────────────────────────────
    public static final String ACTION_ALREADY_MANAGED = "Póliza ya gestionada";
    public static final String ACTION_CLIENT_LOST = "Cliente perdido — fuera de ventana de renovación de %d días. Contactar para nueva contratación.";
    public static final String ACTION_CONTACT_URGENT = "Contactar urgentemente — ventana de renovación de %d días. Quedan %d días.";
    public static final String ACTION_BEFORE_EXPIRY = "Gestionar renovación antes del vencimiento.";

    // ──────────────────────────────────────────────
    //  Cadenas de renovación
    //  Usado en: PolicyServiceImpl.renewPolicy()
    // ──────────────────────────────────────────────
    public static final String RENEWAL_POLICY_SUFFIX = "-R";

    // ──────────────────────────────────────────────
    //  Errores de negocio (PolicyService)
    //  Usado en: PolicyServiceImpl
    // ──────────────────────────────────────────────
    public static final String POLICY_NOT_FOUND = "Policy not found: ";
    public static final String CLIENT_NOT_FOUND = "Client not found: ";
    public static final String ADVISOR_NOT_FOUND = "Advisor not found";
    public static final String ERROR_RENEW_NOT_AUTO = "Solo las pólizas de AUTO pueden renovarse después del vencimiento.";
    public static final String ERROR_RENEW_WINDOW_EXPIRED = "La ventana de renovación de %d días después del vencimiento ha expirado.";

    // ──────────────────────────────────────────────
    //  Errores de importación (ImportService)
    //  Usado en: ImportServiceImpl
    // ──────────────────────────────────────────────
    public static final String ERROR_IMPORT_EMPTY = "El archivo está vacío o no contiene datos válidos.";
    public static final String ERROR_IMPORT_UNSUPPORTED = "Formato de archivo no soportado. Use .xlsx o .xml.";
    public static final String ERROR_IMPORT_INVALID_ROW = "Fila %d: datos inválidos - %s";
    public static final String ERROR_IMPORT_INVALID_TYPE = "Tipo de póliza inválido en fila %d: %s";
    public static final String ERROR_IMPORT_FILE_READ = "Error al leer el archivo: ";
    public static final String ERROR_IMPORT_MISSING_FIELDS = "Campos requeridos faltantes: ";
    public static final String ERROR_IMPORT_INVALID_TYPE_PREFIX = "Tipo de póliza inválido: ";
    public static final String INFO_IMPORT_RESULT = "Importación completada. %d procesados, %d creados, %d errores.";

    // ──────────────────────────────────────────────
    //  Formatos de archivo importables (ImportService)
    //  Usado en: ImportServiceImpl.importClients()
    // ──────────────────────────────────────────────
    public static final String IMPORT_FORMAT_XLSX = "xlsx";
    public static final String IMPORT_FORMAT_XML = "xml";

    // ──────────────────────────────────────────────
    //  Nombres de campo para validación de importación
    //  Usado en: ImportServiceImpl.validateRow()
    // ──────────────────────────────────────────────
    public static final String FIELD_NOMBRE = "nombre";
    public static final String FIELD_NUMERO_POLIZA = "número de póliza";
    public static final String FIELD_TIPO = "tipo";
    public static final String FIELD_ASEGURADORA = "aseguradora";
    public static final String FIELD_FECHA_INICIO = "fecha de inicio";
    public static final String FIELD_FECHA_VENCIMIENTO = "fecha de vencimiento";

    // ──────────────────────────────────────────────
    //  Nombres de vistas Thymeleaf (DashboardController)
    //  Usado en: DashboardController
    // ──────────────────────────────────────────────
    public static final String VIEW_DASHBOARD = "dashboard";
    public static final String VIEW_POLICY_FORM = "policy-form";
}
