package com.onePilates.agendamento.professor.dto;

import com.onePilates.agendamento.model.enums.DiaSemana;
public class KPIsProfessorDTO {

    private Integer qtdAlunosAtendidos;
    private Integer qtdTotalSessoesRealizadas;
    private DiaSemana diaSemanaComMaiorAtendimento;
    private String especialidadeMaisRequisitada;

    public KPIsProfessorDTO() {
    }

    public Integer getQtdAlunosAtendidos() {
        return qtdAlunosAtendidos;
    }

    public void setQtdAlunosAtendidos(Integer qtdAlunosAtendidos) {
        this.qtdAlunosAtendidos = qtdAlunosAtendidos;
    }

    public Integer getQtdTotalSessoesRealizadas() {
        return qtdTotalSessoesRealizadas;
    }

    public void setQtdTotalSessoesRealizadas(Integer qtdTotalSessoesRealizadas) {
        this.qtdTotalSessoesRealizadas = qtdTotalSessoesRealizadas;
    }

    public DiaSemana getDiaSemanaComMaiorAtendimento() {
        return diaSemanaComMaiorAtendimento;
    }

    public void setDiaSemanaComMaiorAtendimento(DiaSemana diaSemanaComMaiorAtendimento) {
        this.diaSemanaComMaiorAtendimento = diaSemanaComMaiorAtendimento;
    }

    public String getEspecialidadeMaisRequisitada() {
        return especialidadeMaisRequisitada;
    }

    public void setEspecialidadeMaisRequisitada(String especialidadeMaisRequisitada) {
        this.especialidadeMaisRequisitada = especialidadeMaisRequisitada;
    }
}
