package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resultado de comparar los patrones de voto entre dos o más políticos (CF-016).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparacionVotosDTO {
    private List<ComparacionPoliticoDTO> politicos;
    private List<ComparacionLeyDTO> leyesComparadas;
    private long leyesEnComun;
    private long coincidencias;      // leyes en común donde todos coincidieron
    private double indiceCoincidencia; // porcentaje de coincidencia (0-100)
}
