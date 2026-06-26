package com.controlf.db.schema;

import com.controlf.db.schema.enums.EstadoLey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "leyes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ley {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String tipoExpediente;
    private String proponente;

    @Column(columnDefinition = "TEXT")
    private String descripcionOriginal;

    @Column(columnDefinition = "TEXT")
    private String descripcionSimplificada;

    @Column(columnDefinition = "TEXT")
    private String impactoSocial;

    private String categoria;

    @Enumerated(EnumType.STRING)
    private EstadoLey estado;

    private LocalDate fechaIngreso;

    @Column(name = "external_id", unique = true)
    private Long externalId;

    @OneToMany(mappedBy = "ley", cascade = CascadeType.ALL)
    private List<Voto> votos;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "ley_comentarios",
        joinColumns = @JoinColumn(name = "ley_id"),
        inverseJoinColumns = @JoinColumn(name = "comentario_id")
    )
    private List<Comentario> comentarios;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "ley_calificaciones",
        joinColumns = @JoinColumn(name = "ley_id"),
        inverseJoinColumns = @JoinColumn(name = "calificacion_id")
    )
    private List<Calificacion> calificaciones;

    @OneToMany(mappedBy = "ley", cascade = CascadeType.ALL)
    private List<VinculoPromesaLey> vinculos;
}
