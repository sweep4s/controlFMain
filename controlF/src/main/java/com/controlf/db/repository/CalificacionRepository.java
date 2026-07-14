package com.controlf.db.repository;

import com.controlf.db.schema.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Integer> {

    @Query("SELECT AVG(c.puntaje) FROM Calificacion c JOIN Ley l ON c MEMBER OF l.calificaciones WHERE l.id = :leyId")
    Double findAveragePuntajeByLeyId(Integer leyId);

    @Query("SELECT AVG(c.puntaje) FROM Calificacion c JOIN Politico p ON c MEMBER OF p.calificaciones WHERE p.id = :politicoId")
    Double findAveragePuntajeByPoliticoId(Integer politicoId);

    @Query("SELECT COUNT(c) FROM Calificacion c JOIN Politico p ON c MEMBER OF p.calificaciones WHERE p.id = :politicoId")
    long countByPoliticoId(Integer politicoId);
}
