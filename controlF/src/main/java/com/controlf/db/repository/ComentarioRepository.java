package com.controlf.db.repository;

import com.controlf.db.schema.Comentario;
import com.controlf.db.schema.enums.EstadoModeracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    List<Comentario> findByEstadoOrderByFechaDesc(EstadoModeracion estado);

    long countByEstado(EstadoModeracion estado);
}
