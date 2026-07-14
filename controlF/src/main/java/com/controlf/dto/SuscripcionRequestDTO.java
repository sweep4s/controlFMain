package com.controlf.dto;

import lombok.Data;

/**
 * Solicitud para crear una suscripción de alertas (CF-015). categoria nula o vacía = todas.
 */
@Data
public class SuscripcionRequestDTO {
    private String categoria;
}
