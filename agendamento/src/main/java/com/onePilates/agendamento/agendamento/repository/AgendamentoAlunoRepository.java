package com.onePilates.agendamento.agendamento.repository;

import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoAlunoRepository extends JpaRepository<AgendamentoAluno, Long> {
    
    @EntityGraph(attributePaths = {"agendamento", "aluno"})
    List<AgendamentoAluno> findByAgendamentoId(Long agendamentoId);
    
    @EntityGraph(attributePaths = {"agendamento", "aluno"})
    Optional<AgendamentoAluno> findByAgendamentoAndAluno(Agendamento agendamento, Aluno aluno);
    
    @EntityGraph(attributePaths = {"agendamento", "aluno"})
    List<AgendamentoAluno> findByAgendamento(Agendamento agendamento);

    /** Aula de um aluno específico; usado pelo portal para garantir que ele só acesse as próprias aulas. */
    Optional<AgendamentoAluno> findByAlunoIdAndAgendamentoId(Long alunoId, Long agendamentoId);

    /** Aulas do aluno no período, da mais antiga para a mais recente. */
    @Query("select aa from AgendamentoAluno aa join fetch aa.agendamento ag "
            + "where aa.aluno.id = :alunoId and ag.dataHora between :inicio and :fim order by ag.dataHora")
    List<AgendamentoAluno> buscarDoAlunoNoPeriodo(@Param("alunoId") Long alunoId,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);

    /** Quantidade de aulas futuras do aluno. */
    @Query("select count(aa) from AgendamentoAluno aa where aa.aluno.id = :alunoId and aa.agendamento.dataHora > :agora")
    long contarFuturas(@Param("alunoId") Long alunoId, @Param("agora") LocalDateTime agora);

    /**
     * Vínculos que ainda precisam do lembrete de confirmação: aula nas próximas horas, aluno com
     * notificações ligadas, presença ainda não confirmada e lembrete ainda não enviado.
     */
    @Query("select aa from AgendamentoAluno aa join fetch aa.agendamento ag join fetch aa.aluno al "
            + "where ag.dataHora > :agora and ag.dataHora <= :limite and aa.lembreteEnviadoEm is null "
            + "and (aa.presencaConfirmada is null or aa.presencaConfirmada = false) and al.notificacaoAtiva = true")
    List<AgendamentoAluno> pendentesDeLembrete(@Param("agora") LocalDateTime agora, @Param("limite") LocalDateTime limite);
}

