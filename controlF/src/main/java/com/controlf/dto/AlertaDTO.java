package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una alerta relevante para un usuario suscrito (CF-015): una nueva ley o votación en una de sus
 * categorías. El indicador "nuevo" marca los ítems posteriores a la fecha de suscripción.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaDTO {
    private String tipo;      // LEY | VOTACION
    private String titulo;
    private String categoria;
    private String fecha;     // yyyy-MM-dd
    private String detalle;
    private String leyId;
    private boolean nuevo;
}
