package com.controlf.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComentarioRequestDTO {
    @NotBlank(message = "El texto del comentario es obligatorio")
    private String texto;

    // Calificación opcional (1-5) enviada junto al comentario para publicarla en el historial (CF-023).
    private Integer puntaje;
}
