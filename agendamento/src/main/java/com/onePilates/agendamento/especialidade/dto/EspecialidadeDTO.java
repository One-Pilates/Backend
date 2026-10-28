package com.onePilates.agendamento.especialidade.dto;

import jakarta.validation.constraints.NotBlank;

public class EspecialidadeDTO {
    @NotBlank
    private String nome;

    public EspecialidadeDTO(String nome) {
        this.nome = nome;
    }

    public EspecialidadeDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
