package com.controlf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionDTO {
    private Integer id;
    private String categoria;  // null = todas las categorías
    private String fechaCreacion;
}
