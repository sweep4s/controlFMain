package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilPoliticoDTO {
    private String id;
    private String nombre;
    private String organizacion;
    private String cargo;
    private String patrimonio;
    private String fotoUrl;
    private String antecedentes;
    private boolean estaActivo;
    private Double porcentajeCoherencia;
    private String estadoEtiqueta;
    // Índice de reputación ciudadana (promedio de calificaciones 1-5) y su etiqueta cualitativa.
    private Double indiceReputacion;
    private long totalCalificaciones;
    private String etiquetaReputacion;
    private List<HistorialCoherenciaDTO> historial;
    // Historial persistente de cambios en patrimonio/antecedentes (CF-005).
    private List<HistorialCambioPerfilDTO> historialCambios;
    private List<ComentarioDebateDTO> comentarios;
}
