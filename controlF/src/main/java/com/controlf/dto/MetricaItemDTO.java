package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Punto genérico de una métrica interactiva (CF-021): una etiqueta y su valor numérico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricaItemDTO {
    private String etiqueta;
    private double valor;
}
