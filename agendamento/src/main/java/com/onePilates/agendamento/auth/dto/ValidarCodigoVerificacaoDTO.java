package com.onePilates.agendamento.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidarCodigoVerificacaoDTO {

    @NotBlank(message = "Preencha o campo email para a prosseguir com a alteração de senha")
    private String email;

    private String codigo;

    public ValidarCodigoVerificacaoDTO() {
    }


    public String getEmail() {
        return email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
