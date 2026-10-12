package com.onePilates.agendamento.funcionario.dto;

import com.onePilates.agendamento.especialidade.dto.EspecialidadeResponseDTO;
import com.onePilates.agendamento.model.Endereco;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class FuncionarioLoginDTO {
    private Long id;
    private String nome;
    private String email;
    private Role role;
    private String cpf;
    private LocalDate dataNascimento;
    private Boolean status;
    private String foto;
    private String observacoes;
    private Boolean notificacaoAtiva;
    private String cargo;
    private Endereco endereco;
    private String telefone;
    private List<EspecialidadeResponseDTO> especialidades;
    private Boolean primeiroAcesso;


    public FuncionarioLoginDTO(Long id, String nome, String email, Role role, String cpf, LocalDate dataNascimento, Boolean status, String foto, String observacoes, Boolean notificacaoAtiva, String cargo, Endereco endereco, String telefone,  Boolean primeiroAcesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.status = status;
        this.foto = foto;
        this.observacoes = observacoes;
        this.notificacaoAtiva = notificacaoAtiva;
        this.cargo = cargo;
        this.endereco = endereco;
        this.telefone = telefone;
        this.primeiroAcesso = primeiroAcesso;
    }

    public FuncionarioLoginDTO(Long id, String nome, String email, Role role, String cpf, LocalDate dataNascimento, Boolean status, String foto, String observacoes, Boolean notificacaoAtiva, String cargo, Endereco endereco, String telefone, List<EspecialidadeResponseDTO> especialidades, Boolean primeiroAcesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.status = status;
        this.foto = foto;
        this.observacoes = observacoes;
        this.notificacaoAtiva = notificacaoAtiva;
        this.cargo = cargo;
        this.endereco = endereco;
        this.telefone = telefone;
        this.especialidades = especialidades;
        this.primeiroAcesso = primeiroAcesso;
    }

    public Boolean getPrimeiroAcesso() {
        return primeiroAcesso;
    }

    public void setPrimeiroAcesso(Boolean primeiroAcesso) {
        this.primeiroAcesso = primeiroAcesso;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
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

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public List<EspecialidadeResponseDTO> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<EspecialidadeResponseDTO> especialidades) {
        this.especialidades = especialidades;
    }


}