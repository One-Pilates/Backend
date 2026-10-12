package com.onePilates.agendamento.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class CriarCodigoValidacaoDTO {

    @NotBlank(message = "Preencha o campo email para a prosseguir com a alteração de senha")
    private String email;

    public CriarCodigoValidacaoDTO() {
    }

    public CriarCodigoValidacaoDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }
}
