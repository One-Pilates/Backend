package com.onePilates.agendamento.pacote.service;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.MovimentoPacote;
import com.onePilates.agendamento.model.PacoteAula;
import com.onePilates.agendamento.model.enums.MotivoMovimentoPacote;
import com.onePilates.agendamento.pacote.dto.MovimentoPacoteResponseDTO;
import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;
import com.onePilates.agendamento.pacote.dto.VerificacaoPacoteDTO;
import com.onePilates.agendamento.pacote.repository.MovimentoPacoteRepository;
import com.onePilates.agendamento.pacote.repository.PacoteAulaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Pacote de aulas por aluno e especialidade.
 * <p>
 * Regras: a aula é debitada ao agendar; só a equipe (secretaria/administrador) define o saldo;
 * agendar sem saldo não é bloqueado, mas gera aviso (e o saldo fica negativo);
 * a aula só volta ao pacote em cancelamento com 24h ou mais, ou quando a equipe retira o aluno da aula.
 */
@Service
public class PacoteAulaService {

    private static final Logger logger = LoggerFactory.getLogger(PacoteAulaService.class);

    private final PacoteAulaRepository pacoteRepository;
    private final MovimentoPacoteRepository movimentoRepository;
    private final AlunoRepository alunoRepository;
    private final EspecialidadeRepository especialidadeRepository;

    public PacoteAulaService(PacoteAulaRepository pacoteRepository, MovimentoPacoteRepository movimentoRepository,
                             AlunoRepository alunoRepository, EspecialidadeRepository especialidadeRepository) {
        this.pacoteRepository = pacoteRepository;
        this.movimentoRepository = movimentoRepository;
        this.alunoRepository = alunoRepository;
        this.especialidadeRepository = especialidadeRepository;
    }

    /** Saldo do aluno em todas as especialidades (0 para as que ainda não têm pacote). */
    @Transactional(readOnly = true)
    public List<PacoteResponseDTO> listar(Long alunoId) {
        if (!alunoRepository.existsById(alunoId)) {
            throw new EntidadeNaoEncontradaException("Aluno não encontrado");
        }
        Map<Long, Integer> saldos = pacoteRepository.findByAlunoId(alunoId).stream()
                .collect(Collectors.toMap(p -> p.getEspecialidade().getId(), PacoteAula::getSaldo));
        return especialidadeRepository.findAll().stream()
                .sorted(Comparator.comparing(Especialidade::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(e -> new PacoteResponseDTO(e.getId(), e.getNome(), saldos.getOrDefault(e.getId(), 0)))
                .toList();
    }

    /** Define o saldo (não pode ser negativo). Só a equipe chama este método, pelo controller. */
    @Transactional
    public PacoteResponseDTO definirSaldo(Long alunoId, Long especialidadeId, Integer aulas) {
        if (aulas == null || aulas < 0) {
            throw new OperacaoInvalidaException("A quantidade de aulas do pacote deve ser zero ou maior.");
        }
        Aluno aluno = buscarAluno(alunoId);
        Especialidade especialidade = buscarEspecialidade(especialidadeId);
        PacoteAula pacote = obterComTrava(aluno, especialidade);
        int variacao = aulas - pacote.getSaldo();
        pacote.setSaldo(aulas);
        pacoteRepository.save(pacote);
        if (variacao != 0) {
            registrar(aluno, especialidade, variacao, aulas, MotivoMovimentoPacote.AJUSTE_MANUAL, null);
        }
        logger.info("Pacote do aluno {} em {} definido para {} aulas por {}",
                alunoId, especialidade.getNome(), aulas, responsavelAtual());
        return new PacoteResponseDTO(especialidade.getId(), especialidade.getNome(), aulas);
    }

    /** Debita 1 aula do pacote da especialidade da aula e marca o vínculo como debitado. */
    @Transactional
    public void debitar(AgendamentoAluno vinculo) {
        if (vinculo.isCreditoDebitado()) {
            return;
        }
        Aluno aluno = vinculo.getAluno();
        Especialidade especialidade = vinculo.getAgendamento().getEspecialidade();
        PacoteAula pacote = obterComTrava(aluno, especialidade);
        pacote.setSaldo(pacote.getSaldo() - 1);
        pacoteRepository.save(pacote);
        vinculo.setCreditoDebitado(true);
        registrar(aluno, especialidade, -1, pacote.getSaldo(), MotivoMovimentoPacote.AGENDAMENTO,
                vinculo.getAgendamento().getId());
    }

    /**
     * Devolve a aula ao pacote da especialidade da aula, se este vínculo a tinha consumido.
     *
     * @return true se houve devolução
     */
    @Transactional
    public boolean devolver(AgendamentoAluno vinculo, MotivoMovimentoPacote motivo) {
        return devolver(vinculo, vinculo.getAgendamento().getEspecialidade(), motivo);
    }

    /**
     * Devolve a aula para uma especialidade explícita. Necessário quando a aula troca de especialidade:
     * o vínculo já aponta para a nova, mas o débito foi feito na antiga.
     *
     * @return true se houve devolução
     */
    @Transactional
    public boolean devolver(AgendamentoAluno vinculo, Especialidade especialidade, MotivoMovimentoPacote motivo) {
        if (!vinculo.isCreditoDebitado()) {
            return false;
        }
        Aluno aluno = vinculo.getAluno();
        PacoteAula pacote = obterComTrava(aluno, especialidade);
        pacote.setSaldo(pacote.getSaldo() + 1);
        pacoteRepository.save(pacote);
        vinculo.setCreditoDebitado(false);
        registrar(aluno, especialidade, 1, pacote.getSaldo(), motivo, vinculo.getAgendamento().getId());
        return true;
    }

    /** Avisos dos alunos que ficaram com saldo negativo na especialidade da aula (depois do débito). */
    @Transactional(readOnly = true)
    public List<String> avisosDeExcedente(Collection<AgendamentoAluno> vinculos) {
        return vinculos.stream()
                .filter(AgendamentoAluno::isCreditoDebitado)
                .map(v -> {
                    Especialidade esp = v.getAgendamento().getEspecialidade();
                    int saldo = pacoteRepository.findByAlunoIdAndEspecialidadeId(v.getAluno().getId(), esp.getId())
                            .map(PacoteAula::getSaldo).orElse(0);
                    return saldo < 0 ? mensagemExcedente(v.getAluno().getNome(), esp.getNome(), saldo) : null;
                })
                .filter(m -> m != null)
                .toList();
    }

    /** Checagem antes de agendar: quem ficaria sem aulas no pacote desta especialidade. */
    @Transactional(readOnly = true)
    public List<VerificacaoPacoteDTO> verificar(Collection<Long> alunoIds, Long especialidadeId) {
        Especialidade especialidade = buscarEspecialidade(especialidadeId);
        Map<Long, Integer> saldos = pacoteRepository.findByAlunoIdIn(alunoIds).stream()
                .filter(p -> p.getEspecialidade().getId().equals(especialidadeId))
                .collect(Collectors.toMap(p -> p.getAluno().getId(), PacoteAula::getSaldo));
        Map<Long, Aluno> alunos = alunoRepository.findAllById(alunoIds).stream()
                .collect(Collectors.toMap(Aluno::getId, Function.identity()));
        return alunoIds.stream()
                .filter(alunos::containsKey)
                .map(id -> {
                    Aluno aluno = alunos.get(id);
                    int saldo = saldos.getOrDefault(id, 0);
                    boolean excede = saldo - 1 < 0;
                    String msg = excede
                            ? String.format("%s não tem aulas disponíveis no pacote de %s (saldo atual: %d). "
                                    + "Esta aula ficará além do pacote.", aluno.getNome(), especialidade.getNome(), saldo)
                            : null;
                    return new VerificacaoPacoteDTO(id, aluno.getNome(), saldo, excede, msg);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimentoPacoteResponseDTO> historico(Long alunoId) {
        if (!alunoRepository.existsById(alunoId)) {
            throw new EntidadeNaoEncontradaException("Aluno não encontrado");
        }
        return movimentoRepository.findByAlunoIdOrderByCriadoEmDesc(alunoId).stream()
                .map(m -> new MovimentoPacoteResponseDTO(m.getEspecialidade().getId(), m.getEspecialidade().getNome(),
                        m.getVariacao(), m.getSaldoApos(), m.getMotivo(), m.getResponsavel(), m.getCriadoEm()))
                .toList();
    }

    /** Remove pacotes e movimentos do aluno (ao excluir o aluno, por causa das chaves estrangeiras). */
    @Transactional
    public void removerDadosDoAluno(Long alunoId) {
        movimentoRepository.deleteByAlunoId(alunoId);
        pacoteRepository.deleteByAlunoId(alunoId);
    }

    public static String mensagemExcedente(String nomeAluno, String especialidade, int saldo) {
        return String.format("%s está além do pacote de %s (saldo: %d).", nomeAluno, especialidade, saldo);
    }

    private PacoteAula obterComTrava(Aluno aluno, Especialidade especialidade) {
        return pacoteRepository.buscarComTrava(aluno.getId(), especialidade.getId())
                .orElseGet(() -> pacoteRepository.save(new PacoteAula(aluno, especialidade, 0)));
    }

    private void registrar(Aluno aluno, Especialidade especialidade, int variacao, int saldoApos,
                           MotivoMovimentoPacote motivo, Long agendamentoId) {
        movimentoRepository.save(new MovimentoPacote(aluno, especialidade, variacao, saldoApos, motivo,
                agendamentoId, responsavelAtual()));
    }

    /** E-mail de quem está logado; "sistema" quando não há usuário (ex.: tarefa da IA em segundo plano). */
    private String responsavelAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getName() != null ? auth.getName() : "sistema";
    }

    private Aluno buscarAluno(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado"));
    }

    private Especialidade buscarEspecialidade(Long id) {
        return especialidadeRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada"));
    }
}
