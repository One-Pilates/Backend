package com.onePilates.agendamento.agendamento.dto;

import com.onePilates.agendamento.aluno.dto.AlunoAgendamentoResponseDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AgendamentoResponseDTO {
    private Long id;
    private LocalDateTime dataHora;
    private String professor;
    private String sala;
    private String especialidade;
    private Set<AlunoAgendamentoResponseDTO> alunos;

    public AgendamentoResponseDTO(Long id, LocalDateTime dataHora, String professor, String sala, String especialidade, Set<AlunoAgendamentoResponseDTO> alunos) {
        this.id = id;
        this.dataHora = dataHora;
        this.professor = professor;
        this.sala = sala;
        this.especialidade = especialidade;
        this.alunos = alunos;
    }

    public AgendamentoResponseDTO() {
    }

    /** Avisos não bloqueantes, por exemplo alunos além do pacote de aulas. */
    private List<String> avisos = new ArrayList<>();

    public List<String> getAvisos() {
        return avisos;
    }

    public void setAvisos(List<String> avisos) {
        this.avisos = avisos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getProfessor() {
        return professor;
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public Set<AlunoAgendamentoResponseDTO> getAlunos() {
        return alunos;
    }

    public void setAlunos(Set<AlunoAgendamentoResponseDTO> alunos) {
        this.alunos = alunos;
    }
}
