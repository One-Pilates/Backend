package com.onePilates.agendamento.pacote.repository;

import com.onePilates.agendamento.model.PacoteAula;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacoteAulaRepository extends JpaRepository<PacoteAula, Long> {

    List<PacoteAula> findByAlunoId(Long alunoId);

    List<PacoteAula> findByAlunoIdIn(Collection<Long> alunoIds);

    Optional<PacoteAula> findByAlunoIdAndEspecialidadeId(Long alunoId, Long especialidadeId);

    /** Leitura com trava: duas secretarias agendando o mesmo aluno ao mesmo tempo não perdem um débito. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PacoteAula p where p.aluno.id = :alunoId and p.especialidade.id = :especialidadeId")
    Optional<PacoteAula> buscarComTrava(@Param("alunoId") Long alunoId, @Param("especialidadeId") Long especialidadeId);

    void deleteByAlunoId(Long alunoId);
}
