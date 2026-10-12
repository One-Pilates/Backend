package com.onePilates.agendamento.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

public class AgendamentoDTO {

    @NotNull(message = "O campo data e hora é obrigatório")
    @Future(message = "A data e hora devem estar no futuro")
    private LocalDateTime dataHora;

    @NotNull(message = "O campo professorId é obrigatório")
    private Long professorId;

    @NotNull(message = "O campo salaId é obrigatório")
    private Long salaId;

    @NotNull(message = "O campo especialidadeId é obrigatório")
    private Long especialidadeId;

    @NotEmpty(message = "É necessário informar ao menos um aluno")
    private Set<@NotNull(message = "ID de aluno não pode ser nulo") Long> alunoIds;

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }

    public Long getEspecialidadeId() {
        return especialidadeId;
    }

    public void setEspecialidadeId(Long especialidadeId) {
        this.especialidadeId = especialidadeId;
    }

    public Set<Long> getAlunoIds() {
        return alunoIds;
    }

    public void setAlunoIds(Set<Long> alunoIds) {
        this.alunoIds = alunoIds;
    }
}
