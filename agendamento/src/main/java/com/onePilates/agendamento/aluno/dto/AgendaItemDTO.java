package com.onePilates.agendamento.aluno.dto;

import java.time.LocalDateTime;

public class AgendaItemDTO {
    private Long agendamentoId;
    private LocalDateTime dataHora;
    private String professor;
    private String sala;
    private String especialidade;
    private Boolean presencaConfirmada;

    public Long getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(Long agendamentoId) { this.agendamentoId = agendamentoId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getProfessor() { return professor; }
    public void setProfessor(String professor) { this.professor = professor; }
    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }
    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public Boolean getPresencaConfirmada() { return presencaConfirmada; }
    public void setPresencaConfirmada(Boolean presencaConfirmada) { this.presencaConfirmada = presencaConfirmada; }
}
