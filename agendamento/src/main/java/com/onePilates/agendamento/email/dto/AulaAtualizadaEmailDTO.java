package com.onePilates.agendamento.email.dto;


import java.util.List;

public class AulaAtualizadaEmailDTO {

    private String nomeProfessor;
    private List<String> nomesAlunos;
    private String destinatario;
    private String dataHoraAgendamento;
    private String nomeSala;
    private String nomeEspecialidade;

    public AulaAtualizadaEmailDTO() {
    }

    public String getNomeProfessor() {
        return nomeProfessor;
    }

    public void setNomeProfessor(String nomeProfessor) {
        this.nomeProfessor = nomeProfessor;
    }

    public List<String> getNomesAlunos() {
        return nomesAlunos;
    }

    public void setNomesAlunos(List<String> nomesAlunos) {
        this.nomesAlunos = nomesAlunos;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
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
