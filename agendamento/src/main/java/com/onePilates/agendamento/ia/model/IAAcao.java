package com.onePilates.agendamento.ia.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Um passo do agente ("Buscando alunos...") que o front mostra enquanto a realização não termina. */
@Entity
@Table(name = "ia_acoes", indexes = {
        @Index(name = "idx_ia_acoes_realizacao", columnList = "realizacaoId")
})
public class IAAcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long realizacaoId;

    @Column(nullable = false)
    private Integer ordem;

    @Column(nullable = false, length = 300)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAcao status;

    private LocalDateTime iniciadaEm;

    private LocalDateTime finalizadaEm;

    public IAAcao() {
    }

    public IAAcao(Long realizacaoId, Integer ordem, String descricao) {
        this.realizacaoId = realizacaoId;
        this.ordem = ordem;
        this.descricao = descricao;
        this.status = StatusAcao.EM_ANDAMENTO;
        this.iniciadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getRealizacaoId() {
        return realizacaoId;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusAcao getStatus() {
        return status;
    }

    public LocalDateTime getIniciadaEm() {
        return iniciadaEm;
    }

    public LocalDateTime getFinalizadaEm() {
        return finalizadaEm;
    }

    public void finalizar(boolean sucesso) {
        this.status = sucesso ? StatusAcao.CONCLUIDA : StatusAcao.ERRO;
        this.finalizadaEm = LocalDateTime.now();
    }
}
