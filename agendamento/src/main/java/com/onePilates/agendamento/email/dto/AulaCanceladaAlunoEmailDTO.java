package com.onePilates.agendamento.email.dto;

public class AulaCanceladaAlunoEmailDTO {

    private String nomeAluno;
    private String dataHoraAgendamento;
    private String nomeSala;
    private String nomeEspecialidade;
    /** true quando o próprio aluno cancelou pelo portal; false quando foi a equipe. */
    private Boolean canceladoPeloAluno;
    /** Se a aula voltou ao pacote do aluno (nulo quando ele não tinha pacote debitado). */
    private Boolean aulaDevolvidaAoPacote;

    public AulaCanceladaAlunoEmailDTO() {
    }

    public Boolean getCanceladoPeloAluno() {
        return canceladoPeloAluno;
    }

    public void setCanceladoPeloAluno(Boolean canceladoPeloAluno) {
        this.canceladoPeloAluno = canceladoPeloAluno;
    }

    public Boolean getAulaDevolvidaAoPacote() {
        return aulaDevolvidaAoPacote;
    }

    public void setAulaDevolvidaAoPacote(Boolean aulaDevolvidaAoPacote) {
        this.aulaDevolvidaAoPacote = aulaDevolvidaAoPacote;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public String getDataHoraAgendamento() {
        return dataHoraAgendamento;
    }

    public void setDataHoraAgendamento(String dataHoraAgendamento) {
        this.dataHoraAgendamento = dataHoraAgendamento;
    }

    public String getNomeSala() {
        return nomeSala;
    }

    public void setNomeSala(String nomeSala) {
        this.nomeSala = nomeSala;
    }

    public String getNomeEspecialidade() {
        return nomeEspecialidade;
    }

    public void setNomeEspecialidade(String nomeEspecialidade) {
        this.nomeEspecialidade = nomeEspecialidade;
    }
}