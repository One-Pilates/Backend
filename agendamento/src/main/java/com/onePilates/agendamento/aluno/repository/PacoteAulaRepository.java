package com.onePilates.agendamento.aluno.repository;

import com.onePilates.agendamento.model.PacoteAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacoteAulaRepository extends JpaRepository<PacoteAula, Long> {

    Optional<PacoteAula> findByAlunoIdAndEspecialidadeId(Long alunoId, Long especialidadeId);

    List<PacoteAula> findByAlunoId(Long alunoId);
}
