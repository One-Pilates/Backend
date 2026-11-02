package com.onePilates.agendamento.aluno.repository;

import com.onePilates.agendamento.model.FeedbackAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackAulaRepository extends JpaRepository<FeedbackAula, Long> {
    Optional<FeedbackAula> findByAgendamentoIdAndAlunoId(Long agendamentoId, Long alunoId);
}
