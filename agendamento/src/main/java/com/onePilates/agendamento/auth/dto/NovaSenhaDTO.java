package com.onePilates.agendamento.auth.dto;

public class NovaSenhaDTO {
    private String senha;
    private String email;

    public NovaSenhaDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
