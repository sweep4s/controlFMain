package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un evento del calendario/agenda legislativa (CF-013), derivado de fechas reales del sistema:
 * el ingreso de un expediente (fechaIngreso) o una votación registrada (fechaVoto).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoAgendaDTO {
    private String tipo;        // INGRESO_LEY | VOTACION
    private String fecha;       // yyyy-MM-dd
    private String titulo;
    private String detalle;
    private String categoria;
    private String estado;
    private String leyId;
    private Long conteoVotos;   // solo para eventos de tipo VOTACION
}
