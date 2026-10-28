package com.onePilates.agendamento.ia.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Conversa de um usuário com o agente; agrupa as realizações e serve de memória para "sim", "confirmo"... */
@Entity
@Table(name = "ia_conversas")
public class IAConversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    private LocalDateTime criadaEm;

    private LocalDateTime ultimoUso;

    public IAConversa() {
    }

    public IAConversa(Long usuarioId) {
        this.usuarioId = usuarioId;
        this.criadaEm = LocalDateTime.now();
        this.ultimoUso = this.criadaEm;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getUltimoUso() {
        return ultimoUso;
    }

    public void setUltimoUso(LocalDateTime ultimoUso) {
        this.ultimoUso = ultimoUso;
    }
}
