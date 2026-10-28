package com.onePilates.agendamento.aluno.dto;

import java.util.List;

public class AlunoPaginadoResponseDTO {
    private List<AlunoResponseDTO> alunos;
    private long totalRegistros;
    private int totalPaginas;

    public AlunoPaginadoResponseDTO() {
    }

    public AlunoPaginadoResponseDTO(List<AlunoResponseDTO> alunos, long totalRegistros, int totalPaginas) {
        this.alunos = alunos;
        this.totalRegistros = totalRegistros;
        this.totalPaginas = totalPaginas;
    }

    public List<AlunoResponseDTO> getAlunos() {
        return alunos;
    }

    public void setAlunos(List<AlunoResponseDTO> alunos) {
        this.alunos = alunos;
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
