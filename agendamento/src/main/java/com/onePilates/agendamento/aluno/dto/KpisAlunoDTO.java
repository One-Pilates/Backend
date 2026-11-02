package com.onePilates.agendamento.aluno.dto;

import java.util.ArrayList;
import java.util.List;

public class KpisAlunoDTO {
    private long aulasNoPeriodo;
    private long presencas;
    private long faltas;
    private long cancelamentos;
    private double taxaPresenca;
    private String especialidadeMaisFrequentada;
    private String diaSemanaMaisFrequente;
    private List<PacoteAulaResponseDTO> aulasRemanescentes = new ArrayList<>();

    public long getAulasNoPeriodo() { return aulasNoPeriodo; }
    public void setAulasNoPeriodo(long aulasNoPeriodo) { this.aulasNoPeriodo = aulasNoPeriodo; }
    public long getPresencas() { return presencas; }
    public void setPresencas(long presencas) { this.presencas = presencas; }
    public long getFaltas() { return faltas; }
    public void setFaltas(long faltas) { this.faltas = faltas; }
    public long getCancelamentos() { return cancelamentos; }
    public void setCancelamentos(long cancelamentos) { this.cancelamentos = cancelamentos; }
    public double getTaxaPresenca() { return taxaPresenca; }
    public void setTaxaPresenca(double taxaPresenca) { this.taxaPresenca = taxaPresenca; }
    public String getEspecialidadeMaisFrequentada() { return especialidadeMaisFrequentada; }
    public void setEspecialidadeMaisFrequentada(String especialidadeMaisFrequentada) { this.especialidadeMaisFrequentada = especialidadeMaisFrequentada; }
    public String getDiaSemanaMaisFrequente() { return diaSemanaMaisFrequente; }
    public void setDiaSemanaMaisFrequente(String diaSemanaMaisFrequente) { this.diaSemanaMaisFrequente = diaSemanaMaisFrequente; }
    public List<PacoteAulaResponseDTO> getAulasRemanescentes() { return aulasRemanescentes; }
    public void setAulasRemanescentes(List<PacoteAulaResponseDTO> aulasRemanescentes) { this.aulasRemanescentes = aulasRemanescentes; }
}
