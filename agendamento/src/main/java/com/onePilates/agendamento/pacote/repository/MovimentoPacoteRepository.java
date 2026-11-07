package com.onePilates.agendamento.pacote.repository;

import com.onePilates.agendamento.model.MovimentoPacote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentoPacoteRepository extends JpaRepository<MovimentoPacote, Long> {

    List<MovimentoPacote> findByAlunoIdOrderByCriadoEmDesc(Long alunoId);

    void deleteByAlunoId(Long alunoId);
}
