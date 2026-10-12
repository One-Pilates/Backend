package com.onePilates.agendamento.ausencia.dto;

import com.onePilates.agendamento.model.enums.DiaSemana;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AusenciaDTO {

    @NotNull(message = "O professor deve ser informado.")
    private Long professorId;

    @NotNull(message = "A data de início é obrigatória.")
    @FutureOrPresent(message = "A data de início não pode estar no passado.")
    private LocalDateTime dataInicio;

    @NotNull(message = "A data de fim é obrigatória.")
    @FutureOrPresent(message = "A data de fim não pode estar no passado.")
    private LocalDateTime dataFim;

    @NotNull(message = "O dia da semana de início é obrigatório.")
    private DiaSemana diaSemanaInicio;

    @NotNull(message = "O dia da semana de fim é obrigatório.")
    private DiaSemana diaSemanaFim;

    @NotBlank(message = "O motivo da ausência é obrigatório.")
    private String motivo;

    public AusenciaDTO() {}

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public DiaSemana getDiaSemanaInicio() {
        return diaSemanaInicio;
    }

    public void setDiaSemanaInicio(DiaSemana diaSemanaInicio) {
        this.diaSemanaInicio = diaSemanaInicio;
    }

    public DiaSemana getDiaSemanaFim() {
        return diaSemanaFim;
    }

    public void setDiaSemanaFim(DiaSemana diaSemanaFim) {
        this.diaSemanaFim = diaSemanaFim;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public boolean isPeriodoValido() {
        return dataInicio != null && dataFim != null && !dataFim.isBefore(dataInicio);
    }
}
