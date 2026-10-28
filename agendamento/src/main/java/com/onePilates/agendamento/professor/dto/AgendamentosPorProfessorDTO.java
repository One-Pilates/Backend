package com.onePilates.agendamento.professor.dto;

public class AgendamentosPorProfessorDTO {

    private Long professorId;
    private String nomeProfessor;
    private Long totalAgendamentosPorProfessor;

    public AgendamentosPorProfessorDTO() {
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public String getNomeProfessor() {
        return nomeProfessor;
    }

    public void setNomeProfessor(String nomeProfessor) {
        this.nomeProfessor = nomeProfessor;
    }

    public Long getTotalAgendamentosPorProfessor() {
        return totalAgendamentosPorProfessor;
    }

    public void setTotalAgendamentosPorProfessor(Long totalAgendamentosPorProfessor) {
        this.totalAgendamentosPorProfessor = totalAgendamentosPorProfessor;
    }
}
