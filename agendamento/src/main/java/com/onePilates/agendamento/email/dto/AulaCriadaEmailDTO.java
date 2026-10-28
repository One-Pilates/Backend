package com.onePilates.agendamento.email.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AulaCriadaEmailDTO{

    private String nomeProfessor;
    private List<String>nomesDosAlunos;
    private String dataHoraAgendamento;
    private String nomeSala;
    private String nomeEspecialidade;

    public AulaCriadaEmailDTO() {
    }

    public String getNomeProfessor() {
        return nomeProfessor;
    }

    public void setNomeProfessor(String nomeProfessor) {
        this.nomeProfessor = nomeProfessor;
    }

    public List<String> getNomesDosAlunos() {
        return nomesDosAlunos;
    }

    public void setNomesDosAlunos(List<String> nomesDosAlunos) {
        this.nomesDosAlunos = nomesDosAlunos;
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
