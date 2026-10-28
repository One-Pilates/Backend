package com.onePilates.agendamento.ausencia.dto;

import com.onePilates.agendamento.model.enums.DiaSemana;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AusenciaResponseDTO {
    private Integer id;
    private String nomeProfessor;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private DiaSemana diaSemanaInicio;
    private DiaSemana diaSemanaFim;
    private String motivo;

    public AusenciaResponseDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeProfessor() {
        return nomeProfessor;
    }

    public void setNomeProfessor(String nomeProfessor) {
        this.nomeProfessor = nomeProfessor;
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
}
