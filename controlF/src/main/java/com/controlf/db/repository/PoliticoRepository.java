package com.controlf.db.repository;

import com.controlf.db.schema.Politico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface PoliticoRepository extends JpaRepository<Politico, Integer>, JpaSpecificationExecutor<Politico> {
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.partidoPolitico FROM Politico p WHERE p.partidoPolitico IS NOT NULL")
    java.util.List<String> findDistinctPartidos();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.region FROM Politico p WHERE p.region IS NOT NULL")
    java.util.List<String> findDistinctRegiones();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p.comision FROM Politico p WHERE p.comision IS NOT NULL")
    java.util.List<String> findDistinctComisiones();

    Optional<Politico> findByNombreCompletoContainingIgnoreCase(String nombre);
}
