package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entrada persistida del historial de cambios de patrimonio/antecedentes de un político (CF-005),
 * derivada del campo historialActualizaciones (JSON) ya almacenado en la base de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCambioPerfilDTO {
    private String campo;
    private String valorAnterior;
    private String valorNuevo;
    private String fecha;
}
