package com.onePilates.agendamento.aluno.repository;

import com.onePilates.agendamento.model.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
	Page<Aluno> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
	Page<Aluno> findByStatus(Boolean status, Pageable pageable);
	Page<Aluno> findByNomeContainingIgnoreCaseAndStatus(String nome, Boolean status, Pageable pageable);
}
