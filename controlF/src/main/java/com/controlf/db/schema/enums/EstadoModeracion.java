package com.controlf.db.schema.enums;

/**
 * Estado de moderación de un contenido ciudadano (comentario) gestionado por el rol VALIDADOR.
 * Solo el contenido APROBADO se publica en las vistas públicas. Los registros existentes o creados
 * antes de esta funcionalidad se tratan como APROBADO (valor por defecto) para no alterar el
 * comportamiento actual de publicación inmediata.
 */
public enum EstadoModeracion {
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    OBSERVADO
}
