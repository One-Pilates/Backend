package com.onePilates.agendamento.ia.repository;

import com.onePilates.agendamento.ia.model.IAAcao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IAAcaoRepository extends JpaRepository<IAAcao, Long> {

    List<IAAcao> findByRealizacaoIdOrderByOrdemAsc(Long realizacaoId);

    int countByRealizacaoId(Long realizacaoId);

    void deleteByRealizacaoIdIn(Collection<Long> realizacaoIds);
}
