package com.onePilates.agendamento.aluno.dto;

public class PacoteAulaResponseDTO {

    private Long alunoId;
    private Long especialidadeId;
    private String especialidade;
    private Integer aulasRemanescentes;

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public Long getEspecialidadeId() {
        return especialidadeId;
    }

    public void setEspecialidadeId(Long especialidadeId) {
        this.especialidadeId = especialidadeId;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public Integer getAulasRemanescentes() {
        return aulasRemanescentes;
    }

    public void setAulasRemanescentes(Integer aulasRemanescentes) {
        this.aulasRemanescentes = aulasRemanescentes;
    }
}
