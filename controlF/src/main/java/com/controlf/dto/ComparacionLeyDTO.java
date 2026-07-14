package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Una ley votada por dos o más de los políticos comparados (CF-016). El mapa votos asocia el id
 * del político con su voto ("FAVOR" / "CONTRA" / "ABSTENCION" o "—" si no votó).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparacionLeyDTO {
    private String leyId;
    private String leyTitulo;
    private Map<String, String> votos;
    private boolean coinciden; // true si todos los que votaron coincidieron en el sentido del voto
}
