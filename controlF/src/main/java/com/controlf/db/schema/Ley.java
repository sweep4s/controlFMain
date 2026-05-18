package com.controlf.db.schema;

import java.time.LocalDate;
import com.controlf.db.schema.enums.EstadoLey;

public class Ley {

    private Integer id;
    private String titulo;
    private String descripcionOriginal;
    private String descripcionSimplificada;
    private String categoria;
    private EstadoLey estado;
    private LocalDate fechaIngreso;

    /* 

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcionOriginal() { return descripcionOriginal; }
    public void setDescripcionOriginal(String descripcionOriginal) { this.descripcionOriginal = descripcionOriginal; }

    public String getDescripcionSimplificada() { return descripcionSimplificada; }
    public void setDescripcionSimplificada(String descripcionSimplificada) { this.descripcionSimplificada = descripcionSimplificada; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public EstadoLey getEstado() { return estado; }
    public void setEstado(EstadoLey estado) { this.estado = estado; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    */
}