package com.onePilates.agendamento.ia.service;

import com.onePilates.agendamento.ia.model.IAAcao;
import com.onePilates.agendamento.ia.repository.IAAcaoRepository;

/** Grava cada passo em ia_acoes; cada save é uma transação própria, então o front já enxerga na próxima consulta. */
public class ProgressoRealizacao implements ProgressoAgente {

    private final Long realizacaoId;
    private final IAAcaoRepository repository;
    private int ordem;

    public ProgressoRealizacao(Long realizacaoId, IAAcaoRepository repository) {
        this.realizacaoId = realizacaoId;
        this.repository = repository;
        this.ordem = repository.countByRealizacaoId(realizacaoId);
    }

    @Override
    public Long iniciar(String descricao) {
        return repository.save(new IAAcao(realizacaoId, ++ordem, descricao)).getId();
    }

    @Override
    public void finalizar(Long acaoId, boolean sucesso) {
        if (acaoId == null) return;
        repository.findById(acaoId).ifPresent(a -> {
            a.finalizar(sucesso);
            repository.save(a);
        });
    }
}
