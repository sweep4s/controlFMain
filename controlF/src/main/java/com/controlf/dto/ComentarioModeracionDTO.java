package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vista de un comentario ciudadano para el panel de validación (CF-029), incluyendo el contexto
 * (ley o político al que pertenece) para que el validador decida aprobar, rechazar u observar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioModeracionDTO {
    private Integer id;
    private String texto;
    private String usuario;
    private String fecha;
    private String estado;
    private String notaModeracion;
    private String contextoTipo;   // LEY | POLITICO | N/D
    private String contextoTitulo;
    private String contextoId;
}
