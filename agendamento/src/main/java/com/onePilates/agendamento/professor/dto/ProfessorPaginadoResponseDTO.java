package com.onePilates.agendamento.professor.dto;

import java.util.List;

public class ProfessorPaginadoResponseDTO {
    private List<ProfessorResponseDTO> professores;
    private long totalRegistros;
    private int totalPaginas;

    public ProfessorPaginadoResponseDTO() {
    }

    public ProfessorPaginadoResponseDTO(List<ProfessorResponseDTO> professores, long totalRegistros, int totalPaginas) {
        this.professores = professores;
        this.totalRegistros = totalRegistros;
        this.totalPaginas = totalPaginas;
    }

    public List<ProfessorResponseDTO> getProfessores() {
        return professores;
    }

    public void setProfessores(List<ProfessorResponseDTO> professores) {
        this.professores = professores;
    }

    public long getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(long totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }
}
