package com.controlf.db.repository;

import com.controlf.db.schema.Ley;
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

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT l.categoria FROM Ley l WHERE l.categoria IS NOT NULL")
    java.util.List<String> findDistinctCategorias();
}
