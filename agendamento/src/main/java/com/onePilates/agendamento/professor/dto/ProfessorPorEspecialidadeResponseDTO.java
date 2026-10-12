package com.onePilates.agendamento.professor.dto;

public class ProfessorPorEspecialidadeResponseDTO {
    private Long id;
    private String nome;

    public ProfessorPorEspecialidadeResponseDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public ProfessorPorEspecialidadeResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
