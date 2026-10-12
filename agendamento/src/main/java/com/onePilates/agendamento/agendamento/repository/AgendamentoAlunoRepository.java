package com.onePilates.agendamento.agendamento.repository;

import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoAlunoRepository extends JpaRepository<AgendamentoAluno, Long> {
    
    @EntityGraph(attributePaths = {"agendamento", "aluno"})
    List<AgendamentoAluno> findByAgendamentoId(Long agendamentoId);
    
    @EntityGraph(attributePaths = {"agendamento", "aluno"})
    Optional<AgendamentoAluno> findByAgendamentoAndAluno(Agendamento agendamento, Aluno aluno);
    
    @EntityGraph(attributePaths = {"agendamento", "aluno"})
    List<AgendamentoAluno> findByAgendamento(Agendamento agendamento);
}

