package com.controlf.db.schema;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Comentario> comentarios;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Calificacion> calificaciones;

    public Usuario(Integer id, String nombre, String email, String passwordHash, String avatarUrl, Rol rol,
                   LocalDateTime fechaRegistro, List<Comentario> comentarios, List<Calificacion> calificaciones) {
        this(id, nombre, email, passwordHash, avatarUrl, rol, fechaRegistro, true, comentarios, calificaciones);
    }

    public Usuario(Integer id, String nombre, String email, String passwordHash, String avatarUrl, Rol rol,
                   LocalDateTime fechaRegistro, boolean activo, List<Comentario> comentarios, List<Calificacion> calificaciones) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.avatarUrl = avatarUrl;
        this.rol = rol;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
        this.comentarios = comentarios;
        this.calificaciones = calificaciones;
    }
        
    @PrePersist
protected void onCreate() {
    if (this.fechaRegistro == null) {
        this.fechaRegistro = LocalDateTime.now();
    }
}
    public enum Rol {
        ADMIN,
        CIUDADANO,
        VALIDADOR
    }
}
