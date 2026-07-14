package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Seguimiento de un debate legislativo (CF-014): incluye el texto oficial (transcripción/motivos)
 * y el resumen simplificado, además del estado del debate y el resultado de la votación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebateLegislativoDTO {
    private String leyId;
    private String titulo;
    private String codigo;
    private String estado;
    private String categoria;
    private String proponente;
    private String fechaIngreso;
    private String resumenOficial;       // descripcionOriginal (transcripción / exposición de motivos)
    private String resumenSimplificado;  // descripcionSimplificada
    private long votosFavor;
    private long votosContra;
    private long votosAbstencion;
    private long totalVotos;
}
