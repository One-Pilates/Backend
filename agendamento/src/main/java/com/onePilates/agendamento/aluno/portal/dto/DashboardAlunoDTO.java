package com.onePilates.agendamento.aluno.portal.dto;

import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Painel simples do aluno. Como não há chamada, "realizada" é toda aula cuja data/hora já passou
 * e que não foi cancelada.
 *
 * @param aulasPerdidas cancelamentos feitos com menos de 24h (a aula continua descontada do pacote)
 * @param aulasAgendadas total de aulas futuras do aluno, independente do período
 * @param saldoTotal    soma dos saldos de todas as especialidades
 */
public record DashboardAlunoDTO(LocalDate de,
                                LocalDate ate,
                                long aulasRealizadas,
                                long aulasCanceladas,
                                long aulasPerdidas,
                                long aulasAgendadas,
                                String especialidadeMaisFrequente,
                                String diaSemanaMaisFrequente,
                                int saldoTotal,
                                List<PacoteResponseDTO> pacotes,
                                List<Contagem> aulasPorEspecialidade,
                                List<Contagem> aulasPorDiaSemana,
                                AulaAlunoDTO proximaAula) {

    public record Contagem(String nome, long quantidade) {
    }
}
