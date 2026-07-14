package com.controlf.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * Solicitud para registrar manualmente una nueva propuesta de ley (CF-007).
 */
@Data
public class CrearLeyRequestDTO {
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El código de expediente es obligatorio")
    private String codigo;

    private String tipoExpediente;
    private String proponente;
    private String descripcionOriginal;
    private String descripcionSimplificada;
    private String impactoSocial;
    private String categoria;
    private String estado;         // EstadoLey; por defecto DEBATE
    private LocalDate fechaIngreso;
}
