package com.onePilates.agendamento.aluno.service;

import com.onePilates.agendamento.aluno.dto.PacoteAulaResponseDTO;
import com.onePilates.agendamento.aluno.repository.PacoteAulaRepository;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.PacoteAula;
import com.onePilates.agendamento.model.enums.StatusPresenca;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
public class PacoteAulaService {

    private final PacoteAulaRepository pacoteAulaRepository;
    private final AlunoRepository alunoRepository;
    private final EspecialidadeRepository especialidadeRepository;

    public PacoteAulaService(PacoteAulaRepository pacoteAulaRepository, AlunoRepository alunoRepository,
                             EspecialidadeRepository especialidadeRepository) {
        this.pacoteAulaRepository = pacoteAulaRepository;
        this.alunoRepository = alunoRepository;
        this.especialidadeRepository = especialidadeRepository;
    }

    @Transactional
    public PacoteAula definir(Long alunoId, Long especialidadeId, int aulasRemanescentes) {
        if (aulasRemanescentes < 0) {
            throw new OperacaoInvalidaException("O saldo de aulas não pode ser negativo.");
        }
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado"));
        Especialidade especialidade = especialidadeRepository.findById(especialidadeId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada"));

        PacoteAula pacote = pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(alunoId, especialidadeId)
                .orElseGet(PacoteAula::new);
        pacote.setAluno(aluno);
        pacote.setEspecialidade(especialidade);
        pacote.setAulasRemanescentes(aulasRemanescentes);
        return pacoteAulaRepository.save(pacote);
    }

    public List<PacoteAulaResponseDTO> listar(Long alunoId) {
        return pacoteAulaRepository.findByAlunoId(alunoId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void aplicarPresenca(AgendamentoAluno vinculo) {
        if (vinculo.getStatusPresenca() == StatusPresenca.PRESENTE) {
            debitar(vinculo, vinculo.getAgendamento().getEspecialidade());
            return;
        }
        if (Boolean.TRUE.equals(vinculo.getCreditoConsumido())) {
            estornar(vinculo);
        }
    }

    @Transactional
    public void estornar(AgendamentoAluno vinculo) {
        if (!Boolean.TRUE.equals(vinculo.getCreditoConsumido())) {
            return;
        }
        devolver(vinculo.getAluno().getId(), vinculo.getAgendamento().getEspecialidade().getId());
        vinculo.setCreditoConsumido(false);
    }

    @Transactional
    public void trocarEspecialidade(Collection<AgendamentoAluno> vinculos, Especialidade antiga, Especialidade nova) {
        if (antiga.getId().equals(nova.getId())) {
            return;
        }
        for (AgendamentoAluno vinculo : vinculos) {
            if (vinculo.getStatusPresenca() != StatusPresenca.PRESENTE) {
                continue;
            }
            if (Boolean.TRUE.equals(vinculo.getCreditoConsumido())) {
                devolver(vinculo.getAluno().getId(), antiga.getId());
                vinculo.setCreditoConsumido(false);
            }
            debitar(vinculo, nova);
        }
    }

    private void debitar(AgendamentoAluno vinculo, Especialidade especialidade) {
        if (Boolean.TRUE.equals(vinculo.getCreditoConsumido())) {
            return;
        }
        PacoteAula pacote = pacoteAulaRepository
                .findByAlunoIdAndEspecialidadeId(vinculo.getAluno().getId(), especialidade.getId())
                .orElse(null);
        if (pacote == null || pacote.getAulasRemanescentes() == null || pacote.getAulasRemanescentes() <= 0) {
            return;
        }
        pacote.setAulasRemanescentes(pacote.getAulasRemanescentes() - 1);
        vinculo.setCreditoConsumido(true);
        pacoteAulaRepository.save(pacote);
    }

    private void devolver(Long alunoId, Long especialidadeId) {
        PacoteAula pacote = pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(alunoId, especialidadeId).orElse(null);
        if (pacote == null) {
            return;
        }
        int saldo = pacote.getAulasRemanescentes() == null ? 0 : pacote.getAulasRemanescentes();
        pacote.setAulasRemanescentes(saldo + 1);
        pacoteAulaRepository.save(pacote);
    }

    private PacoteAulaResponseDTO toResponse(PacoteAula pacote) {
        PacoteAulaResponseDTO dto = new PacoteAulaResponseDTO();
        dto.setAlunoId(pacote.getAluno().getId());
        dto.setEspecialidadeId(pacote.getEspecialidade().getId());
        dto.setEspecialidade(pacote.getEspecialidade().getNome());
        dto.setAulasRemanescentes(pacote.getAulasRemanescentes());
        return dto;
    }
}
