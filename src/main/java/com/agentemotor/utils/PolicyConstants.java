package com.agentemotor.utils;

public final class PolicyConstants {

    private PolicyConstants() {}

    public static final Long DEFAULT_ADVISOR_ID = 1L;

    public static final int RENEWAL_WINDOW_DAYS = 30;
    public static final int URGENT_THRESHOLD_DAYS = 7;

    public static final String POLICY_NOT_FOUND = "Policy not found: ";
    public static final String CLIENT_NOT_FOUND = "Client not found: ";
    public static final String ADVISOR_NOT_FOUND = "Advisor not found";

    public static final String PRIORITY_COMPLETADA = "completada";
    public static final String PRIORITY_PERDIDO = "perdido";
    public static final String PRIORITY_URGENTE = "urgente";
    public static final String PRIORITY_ALTA = "alta";
    public static final String PRIORITY_MEDIA = "media";
    public static final String PRIORITY_BAJA = "baja";

    public static final String ACTION_ALREADY_MANAGED = "Póliza ya gestionada";
    public static final String ACTION_CLIENT_LOST = "Cliente perdido — fuera de ventana de renovación de %d días. Contactar para nueva contratación.";
    public static final String ACTION_CONTACT_URGENT = "Contactar urgentemente — ventana de renovación de %d días. Quedan %d días.";
    public static final String ACTION_BEFORE_EXPIRY = "Gestionar renovación antes del vencimiento.";
}
