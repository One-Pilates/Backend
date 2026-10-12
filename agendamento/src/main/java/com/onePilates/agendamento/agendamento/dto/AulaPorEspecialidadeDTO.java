package com.onePilates.agendamento.agendamento.dto;

public class AulaPorEspecialidadeDTO {
    private Long professorId;
    private String especialidade;
    private Double percentualAulas;

    // Construtor padrão (necessário para Spring Data JPA)
    public AulaPorEspecialidadeDTO() {
    }

    // Construtor com parâmetros (usado pelo Spring Data JPA para mapear resultados)
    public AulaPorEspecialidadeDTO(Long professorId, String especialidade, Double percentualAulas) {
        this.professorId = professorId;
        this.especialidade = especialidade;
        this.percentualAulas = percentualAulas;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public Double getPercentualAulas() {
        return percentualAulas;
    }

    public void setPercentualAulas(Double percentualAulas) {
        this.percentualAulas = percentualAulas;
    }
}

