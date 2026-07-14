package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resumen del patrón de votación de un político dentro de una comparación (CF-016).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparacionPoliticoDTO {
    private String id;
    private String nombre;
    private String organizacion;
    private String fotoUrl;
    private long totalVotos;
    private long votosFavor;
    private long votosContra;
    private long votosAbstencion;
    private long asistencias;
    private long inasistencias;
    private double porcentajeAsistencia;
    private double porcentajeCoherencia;
}
