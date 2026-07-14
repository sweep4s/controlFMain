package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoLeyDTO {
    private String id;
    private String titulo;
    private String resumenEjecutivo;
    private String impactoSocial;
    // Estado y categoría reales de la ley (fuente de verdad: BD) para dar trazabilidad en el perfil.
    private String estado;
    private String categoria;
}
