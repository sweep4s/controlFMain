package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioDebateDTO {
    private String id;
    private String usuario;
    private String fecha;
    private String mensaje;
    private String avatarUrl;
    // Calificación ciudadana (1-5) asociada al comentario, si existe (CF-023).
    private Integer puntaje;
}
