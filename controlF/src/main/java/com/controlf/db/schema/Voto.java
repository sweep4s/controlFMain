package com.controlf.db.schema;

import com.controlf.db.schema.enums.TipoVoto;
import java.time.LocalDateTime;

public class Voto {

    private Integer id;
    private TipoVoto tipoVoto;
    private Boolean asistencia;
    private LocalDateTime fechaVoto;

    /* 

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public TipoVoto getTipoVoto() {
        return tipoVoto;
    }

    public void setTipoVoto(TipoVoto tipoVoto) {
        this.tipoVoto = tipoVoto;
    }

    public Boolean getAsistencia() { return asistencia; }
    public void setAsistencia(Boolean asistencia) { this.asistencia = asistencia; }

    public LocalDateTime getFechaVoto() { return fechaVoto; }
    public void setFechaVoto(LocalDateTime fechaVoto) { this.fechaVoto = fechaVoto; }
    */
}