package com.onePilates.agendamento.agendamento.dto;

public class AgendamentoPorDiaDTO {
    private String diaSemana;
    private Long totalAgendamentos;

    // Construtor padrão (necessário para Spring Data JPA)
    public AgendamentoPorDiaDTO() {
    }

    // Construtor com parâmetros (usado pelo Spring Data JPA para mapear resultados)
    public AgendamentoPorDiaDTO(String diaSemana, Long totalAgendamentos) {
        this.diaSemana = diaSemana;
        this.totalAgendamentos = totalAgendamentos;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Long getTotalAgendamentos() {
        return totalAgendamentos;
    }

    public void setTotalAgendamentos(Long totalAgendamentos) {
        this.totalAgendamentos = totalAgendamentos;
    }
}

