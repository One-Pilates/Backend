package com.onePilates.agendamento.ausencia.repository;

import com.onePilates.agendamento.model.Ausencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AusenciaRepository extends JpaRepository<Ausencia, Integer> {
    
    @EntityGraph(attributePaths = {"professor"})
    List<Ausencia> findByProfessorId(Long professorId);
}