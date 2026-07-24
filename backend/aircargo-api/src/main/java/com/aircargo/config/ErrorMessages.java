package com.aircargo.config;

public final class ErrorMessages {

    private ErrorMessages() {}

    // Auth
    public static final String USER_NOT_FOUND = "Usuario no encontrado";
    public static final String USER_INACTIVE = "Usuario inactivo";
    public static final String ACCOUNT_LOCKED = "Cuenta bloqueada por intentos fallidos. Intente de nuevo después de %s.";
    public static final String ACCOUNT_LOCKED_5_ATTEMPTS = "Cuenta bloqueada por 5 intentos fallidos. Intente de nuevo en 30 minutos.";
    public static final String WRONG_PASSWORD = "Contraseña incorrecta. Intentos restantes: %d";
    public static final String PASSWORD_REQUIRED = "Se requiere la contraseña actual para cambiarla";
    public static final String CURRENT_PASSWORD_WRONG = "Contraseña actual incorrecta";
    public static final String NO_PREVIOUS_PASSWORD = "Este usuario no tiene contraseña previa. No envíe contraseña actual.";
    public static final String MFA_REQUIRED = "Se requiere código de autenticación de dos factores";
    public static final String MFA_CODE_INVALID = "Código de autenticación inválido";
    public static final String MFA_ACCOUNT_LOCKED = "Cuenta bloqueada por intentos fallidos de MFA. Contacte al administrador.";
    public static final String MFA_NOT_CONFIGURED = "Debes configurar autenticación de dos factores antes de cambiar tu contraseña";
    public static final String MFA_LOCKED = "Cuenta bloqueada por intentos fallidos de MFA";
    public static final String TOTP_INVALID = "Código TOTP inválido";
    public static final String CURRENT_PASSWORD_NEEDED = "Se requiere la contraseña actual";
    public static final String REFRESH_TOKEN_REQUIRED = "refreshToken es requerido";
    public static final String REFRESH_TOKEN_REVOCADO = "Token de refresco revocado. Inicie sesión de nuevo.";
    public static final String REFRESH_TOKEN_EXPIRED = "Token de refresco expirado o inválido";
    public static final String REFRESH_TOKEN_WRONG_TYPE = "Token inválido: se esperaba un token de refresco";
    public static final String REFRESH_TOKEN_INVALID = "Token de refresco inválido";

    // Scan
    public static final String CODE_NOT_RECOGNIZED = "Código no reconocido: %s";
    public static final String ULD_NOT_FOUND = "ULD no encontrada: %s";
    public static final String PIECE_LIMIT_REACHED = "Límite alcanzado: %d/%d piezas para %s";
    public static final String NO_PIECES_TO_UNDO = "No hay piezas para deshacer";
    public static final String LAST_PIECE_REMOVED = "Última pieza eliminada";

    // Warehouse
    public static final String RECEIPT_DATA_INCOMPLETE = "DATOS INCOMPLETOS: El recibo debe contener al menos una pieza para cubicar.";
    public static final String RECEIPT_DATA_INCOMPLETE_SHORT = "DATOS INCOMPLETOS";
    public static final String RECEIPT_PROCESS_ERROR = "Error procesando el recibo en bodega: %s";
    public static final String RECEIPT_UPDATE_ERROR = "Error actualizando recibo: %s";
    public static final String RECEIPT_VALIDATION_ERROR = "Error validando: %s";
    public static final String RECEIPT_NOT_FOUND = "Recibo no encontrado: %s";
    public static final String RECEIPT_EXPORT_ERROR = "Error exportando recibo: %s";
    public static final String RECEIPT_PDF_ERROR = "Error generando PDF del recibo: %s";
    public static final String RECEIPT_EVIDENCE_JSON_ERROR = "Error obteniendo evidencias: %s";
    public static final String RECEIPT_EVIDENCE_HTML_ERROR = "Error generando documento de evidencias: %s";
    public static final String RECEIPT_EVIDENCE_PDF_ERROR = "Error generando PDF de evidencias: %s";
    public static final String RECEIPT_EXPORT_URL_ERROR = "Error generando URL de exportación: %s";
}
