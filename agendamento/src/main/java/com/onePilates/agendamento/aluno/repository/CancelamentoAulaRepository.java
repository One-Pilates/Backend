package com.onePilates.agendamento.aluno.repository;

import com.onePilates.agendamento.model.CancelamentoAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CancelamentoAulaRepository extends JpaRepository<CancelamentoAula, Long> {

    List<CancelamentoAula> findByAlunoIdAndDataHoraAulaBetweenOrderByDataHoraAulaAsc(
            Long alunoId, LocalDateTime inicio, LocalDateTime fim);

    void deleteByAlunoId(Long alunoId);
}
