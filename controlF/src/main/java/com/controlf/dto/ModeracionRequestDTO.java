package com.controlf.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Solicitud del validador para cambiar el estado de moderación de un comentario (CF-029).
 * estado admite: APROBADO, RECHAZADO, OBSERVADO, PENDIENTE.
 */
@Data
public class ModeracionRequestDTO {
    @NotBlank(message = "El estado de moderación es obligatorio")
    private String estado;

    private String nota;
}
