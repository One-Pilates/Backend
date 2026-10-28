package com.onePilates.agendamento.ia.repository;

import com.onePilates.agendamento.ia.model.IARealizacao;
import com.onePilates.agendamento.ia.model.StatusRealizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IARealizacaoRepository extends JpaRepository<IARealizacao, Long> {

    /** Últimas realizações já respondidas da conversa (da mais recente para a mais antiga). */
    List<IARealizacao> findTop3ByConversaIdAndStatusOrderByIdDesc(Long conversaId, StatusRealizacao status);

    /** Realizações do usuário ainda na fila ou em execução. */
    List<IARealizacao> findByUsuarioIdAndStatusIn(Long usuarioId, Collection<StatusRealizacao> status);
}
