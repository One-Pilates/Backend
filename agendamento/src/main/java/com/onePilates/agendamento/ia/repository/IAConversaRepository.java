package com.onePilates.agendamento.ia.repository;

import com.onePilates.agendamento.ia.model.IAConversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IAConversaRepository extends JpaRepository<IAConversa, Long> {

    Optional<IAConversa> findTopByUsuarioIdOrderByUltimoUsoDesc(Long usuarioId);

    List<IAConversa> findByUsuarioIdOrderByUltimoUsoDesc(Long usuarioId);
}
