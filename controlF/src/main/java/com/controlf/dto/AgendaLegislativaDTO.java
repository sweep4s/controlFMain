package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agenda / calendario legislativo (CF-013): eventos ordenados por fecha con totales de apoyo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendaLegislativaDTO {
    private List<EventoAgendaDTO> eventos;
    private long totalEventos;
    private long totalIngresos;
    private long totalVotaciones;
}
