package com.onePilates.agendamento.funcionario.repository;

import com.onePilates.agendamento.model.Funcionario;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario,Long> {

    Optional<Funcionario> findByEmail(String email);

    long countByRole(Role role);

    long countByRoleAndStatus(Role role, Boolean status);

}
