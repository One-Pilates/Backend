package com.onePilates.agendamento.ia.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Um pedido ao agente: nasce PENDENTE no endpoint, é processado pelo consumidor da fila e termina CONCLUIDO ou ERRO. */
@Entity
@Table(name = "ia_realizacoes", indexes = {
        @Index(name = "idx_ia_realizacoes_conversa", columnList = "conversaId")
})
public class IARealizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dono do pedido: o consumidor não tem JWT, então monta o contexto do usuário a partir deste id. */
    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Long conversaId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Column(columnDefinition = "TEXT")
    private String promptMelhorado;

    @Column(columnDefinition = "TEXT")
    private String resposta;

    @Column(length = 1000)
    private String erro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusRealizacao status;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    public IARealizacao() {
    }

    public IARealizacao(Long usuarioId, Long conversaId, String mensagem) {
        this.usuarioId = usuarioId;
        this.conversaId = conversaId;
        this.mensagem = mensagem;
        this.status = StatusRealizacao.PENDENTE;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = this.criadoEm;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getConversaId() {
        return conversaId;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getPromptMelhorado() {
        return promptMelhorado;
    }

    public void setPromptMelhorado(String promptMelhorado) {
        this.promptMelhorado = promptMelhorado;
        this.atualizadoEm = LocalDateTime.now();
    }

    public String getResposta() {
        return resposta;
    }

    public String getErro() {
        return erro;
    }

    public StatusRealizacao getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void iniciar() {
        this.status = StatusRealizacao.PROCESSANDO;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void concluir(String resposta) {
        this.status = StatusRealizacao.CONCLUIDO;
        this.resposta = resposta;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void falhar(String erro) {
        this.status = StatusRealizacao.ERRO;
        this.erro = erro != null && erro.length() > 1000 ? erro.substring(0, 1000) : erro;
        this.atualizadoEm = LocalDateTime.now();
    }
}
