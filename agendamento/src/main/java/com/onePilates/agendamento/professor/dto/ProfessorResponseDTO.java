package com.onePilates.agendamento.professor.dto;

import com.onePilates.agendamento.endereco.dto.EnderecoResponseDTO;
import com.onePilates.agendamento.especialidade.dto.EspecialidadeResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class ProfessorResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private LocalDate idade;
    private Boolean status;
    private String foto;
    private String observacoes;
    private Boolean notificacaoAtiva;
    private String cargo;
    private String role;
    private EnderecoResponseDTO endereco;
    private String telefone;
    private Set<EspecialidadeResponseDTO> especialidades;


    public ProfessorResponseDTO() {
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getIdade() {
        return idade;
    }

    public void setIdade(LocalDate idade) {
        this.idade = idade;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Boolean getNotificacaoAtiva() {
        return notificacaoAtiva;
    }

    public void setNotificacaoAtiva(Boolean notificacaoAtiva) {
        this.notificacaoAtiva = notificacaoAtiva;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public EnderecoResponseDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoResponseDTO endereco) {
        this.endereco = endereco;
    }

    public Set<EspecialidadeResponseDTO> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(Set<EspecialidadeResponseDTO> especialidades) {
        this.especialidades = especialidades;
    }

}