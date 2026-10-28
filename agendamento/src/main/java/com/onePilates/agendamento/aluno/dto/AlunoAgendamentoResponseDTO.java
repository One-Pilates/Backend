package com.onePilates.agendamento.aluno.dto;

public class AlunoAgendamentoResponseDTO {
    private Long id;
    private String nome;
    private String observacao;
    private Boolean status;
    private Boolean alunoComLimitacoesFisicas;

    public AlunoAgendamentoResponseDTO() {
    }

    public AlunoAgendamentoResponseDTO(Long id, String nome, String observacao, Boolean status) {
        this.id = id;
        this.nome = nome;
        this.observacao = observacao;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getAlunoComLimitacoesFisicas() {
        return alunoComLimitacoesFisicas;
    }

    public void setAlunoComLimitacoesFisicas(Boolean alunoComLimitacoesFisicas) {
        this.alunoComLimitacoesFisicas = alunoComLimitacoesFisicas;
    }
}
