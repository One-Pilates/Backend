package com.onePilates.agendamento.auth.dto;

import com.onePilates.agendamento.funcionario.dto.FuncionarioLoginDTO;
import java.time.LocalDate;

public class LoginResponseDTO {

    private String token;
    private String role;

    private FuncionarioLoginDTO funcionario;

    public LoginResponseDTO(String token, String role, FuncionarioLoginDTO funcionario) {
        this.token = token;
        this.role = role;
        this.funcionario = funcionario;
    }

    public LoginResponseDTO() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public FuncionarioLoginDTO getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(FuncionarioLoginDTO funcionario) {
        this.funcionario = funcionario;
    }
}
