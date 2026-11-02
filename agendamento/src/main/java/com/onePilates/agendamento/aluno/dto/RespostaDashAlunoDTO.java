package com.onePilates.agendamento.aluno.dto;

import com.onePilates.agendamento.agendamento.dto.AgendamentoPorDiaDTO;
import com.onePilates.agendamento.agendamento.dto.AulaPorEspecialidadeDTO;

import java.util.ArrayList;
import java.util.List;

public class RespostaDashAlunoDTO {
    private List<HistoricoAlunoDTO> historico = new ArrayList<>();
    private List<AgendamentoPorDiaDTO> aulasPorDiaSemana = new ArrayList<>();
    private List<AulaPorEspecialidadeDTO> aulasPorEspecialidade = new ArrayList<>();
    private KpisAlunoDTO kpis = new KpisAlunoDTO();

    public List<HistoricoAlunoDTO> getHistorico() { return historico; }
    public void setHistorico(List<HistoricoAlunoDTO> historico) { this.historico = historico; }
    public List<AgendamentoPorDiaDTO> getAulasPorDiaSemana() { return aulasPorDiaSemana; }
    public void setAulasPorDiaSemana(List<AgendamentoPorDiaDTO> aulasPorDiaSemana) { this.aulasPorDiaSemana = aulasPorDiaSemana; }
    public List<AulaPorEspecialidadeDTO> getAulasPorEspecialidade() { return aulasPorEspecialidade; }
    public void setAulasPorEspecialidade(List<AulaPorEspecialidadeDTO> aulasPorEspecialidade) { this.aulasPorEspecialidade = aulasPorEspecialidade; }
    public KpisAlunoDTO getKpis() { return kpis; }
    public void setKpis(KpisAlunoDTO kpis) { this.kpis = kpis; }
}
