package com.onePilates.agendamento.model;

import com.onePilates.agendamento.model.enums.DiaSemana;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Ausencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Professor professor;

    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;

    @Enumerated(EnumType.STRING)
    private DiaSemana diaSemanaInicio;

    @Enumerated(EnumType.STRING)
    private DiaSemana diaSemanaFim;

    private String motivo;

 public Ausencia(Integer id, Professor professor, LocalDateTime dataInicio, LocalDateTime dataFim, DiaSemana diaSemanaInicio, DiaSemana diaSemanaFim, String motivo) {
        this.id = id;
        this.professor = professor;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.diaSemanaInicio = diaSemanaInicio;
        this.diaSemanaFim = diaSemanaFim;
        this.motivo = motivo;
    }

    public Ausencia() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
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