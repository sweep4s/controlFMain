package com.controlf.db.schema;

import com.controlf.db.schema.enums.EstadoModeracion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private Boolean esBasadoEnHechos;
    private LocalDateTime fecha;

    // Calificación ciudadana (1-5) asociada al comentario para mostrarla en el historial (CF-023).
    private Integer puntaje;

    // Estado de moderación gestionado por el rol VALIDADOR (CF-029). Por defecto APROBADO
    // para mantener la publicación inmediata actual; los validadores pueden despublicarlo.
    @Enumerated(EnumType.STRING)
    private EstadoModeracion estado = EstadoModeracion.APROBADO;

    // Observación opcional que deja el validador al revisar el comentario.
    @Column(columnDefinition = "TEXT")
    private String notaModeracion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /**
     * Constructor de compatibilidad previo a la moderación (CF-029): mantiene la firma usada por el
     * seeder y asume el estado APROBADO para no alterar el comportamiento existente.
     */
    public Comentario(Integer id, String texto, Boolean esBasadoEnHechos, LocalDateTime fecha, Usuario usuario) {
        this.id = id;
        this.texto = texto;
        this.esBasadoEnHechos = esBasadoEnHechos;
        this.fecha = fecha;
        this.usuario = usuario;
        this.estado = EstadoModeracion.APROBADO;
    }
}
