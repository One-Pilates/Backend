package com.onePilates.agendamento.administrador.repository;

import com.onePilates.agendamento.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
