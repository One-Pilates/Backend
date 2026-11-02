package com.onePilates.agendamento.aluno.dto;

import java.time.LocalDateTime;

public class HistoricoAlunoDTO {
    private LocalDateTime dataHora;
    private String especialidade;
    private String professor;
    private String sala;
    private String situacao;

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public String getProfessor() { return professor; }
    public void setProfessor(String professor) { this.professor = professor; }
    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }
    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }
}
