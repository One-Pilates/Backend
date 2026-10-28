package com.onePilates.agendamento.sala.repository;

import com.onePilates.agendamento.model.Sala;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {

    @EntityGraph(attributePaths = {"especialidades"})
    @Override
    Optional<Sala> findById(Long id);

    @EntityGraph(attributePaths = {"especialidades"})
    @Override
    List<Sala> findAll();

    @EntityGraph(attributePaths = {"especialidades"})
    List<Sala> findByEspecialidadesId(Long Id);
}
