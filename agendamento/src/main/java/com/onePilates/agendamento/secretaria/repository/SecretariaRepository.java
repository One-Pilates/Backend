package com.onePilates.agendamento.secretaria.repository;

import com.onePilates.agendamento.model.Secretaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecretariaRepository extends JpaRepository<Secretaria, Long> {
    Optional<Secretaria> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
