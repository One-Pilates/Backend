package com.onePilates.agendamento.aluno.dto;

public class AtualizarObservacaoAlunoDTO {
    private String observacao;

    public AtualizarObservacaoAlunoDTO() {
    }

    public AtualizarObservacaoAlunoDTO(String observacao) {
        this.observacao = observacao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
