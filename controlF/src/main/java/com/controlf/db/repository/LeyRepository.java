package com.controlf.db.repository;

import com.controlf.db.schema.Ley;
import com.controlf.db.schema.enums.EstadoLey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface LeyRepository extends JpaRepository<Ley, Integer>, JpaSpecificationExecutor<Ley> {
    Optional<Ley> findByCodigo(String codigo);

    boolean existsByExternalId(Long externalId);
    Optional<Ley> findByExternalId(Long externalId);

    long countByProponente(String proponente);

    long countByEstado(EstadoLey estado);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT l.categoria FROM Ley l WHERE l.categoria IS NOT NULL")
    java.util.List<String> findDistinctCategorias();

    // Reverse lookup del comentario ciudadano a su ley (para dar contexto en el panel de validación).
    @org.springframework.data.jpa.repository.Query("SELECT l FROM Ley l JOIN l.comentarios c WHERE c.id = :comentarioId")
    Optional<Ley> findByComentarioId(Integer comentarioId);
}
