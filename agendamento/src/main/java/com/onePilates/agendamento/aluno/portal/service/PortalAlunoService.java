package com.onePilates.agendamento.aluno.portal.service;

import com.onePilates.agendamento.agendamento.repository.AgendamentoAlunoRepository;
import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.aluno.portal.dto.AtualizarPerfilAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.AulaAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.CancelamentoResultadoDTO;
import com.onePilates.agendamento.aluno.portal.dto.DashboardAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.PerfilAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.SituacaoAulaAluno;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.repository.CancelamentoAulaRepository;
import com.onePilates.agendamento.email.dto.AulaAtualizadaEmailDTO;
import com.onePilates.agendamento.email.dto.AulaCanceladaAlunoEmailDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.CancelamentoAula;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.enums.MotivoMovimentoPacote;
import com.onePilates.agendamento.model.enums.TipoEmail;
import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;
import com.onePilates.agendamento.pacote.service.PacoteAulaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Regras do portal do aluno. O aluno é sempre identificado pelo e-mail do JWT: nenhuma operação
 * aceita um id de aluno vindo do cliente, então ele só enxerga e altera o que é dele.
 */
@Service
public class PortalAlunoService {

    private static final Logger logger = LoggerFactory.getLogger(PortalAlunoService.class);

    /** Cancelar com esta antecedência (ou mais) devolve a aula ao pacote. */
    public static final Duration ANTECEDENCIA_CANCELAMENTO = Duration.ofHours(24);
    public static final int DURACAO_AULA_MINUTOS = 60;
    private static final long LIMITE_DIAS_CONSULTA = 366;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final AlunoRepository alunoRepository;
    private final AgendamentoAlunoRepository agendamentoAlunoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final CancelamentoAulaRepository cancelamentoRepository;
    private final PacoteAulaService pacoteService;
    private final AgendamentoService agendamentoService;
    private final RabbitMQProducer rabbitMQ;

    public PortalAlunoService(AlunoRepository alunoRepository, AgendamentoAlunoRepository agendamentoAlunoRepository,
                              AgendamentoRepository agendamentoRepository, CancelamentoAulaRepository cancelamentoRepository,
                              PacoteAulaService pacoteService, AgendamentoService agendamentoService,
                              RabbitMQProducer rabbitMQ) {
        this.alunoRepository = alunoRepository;
        this.agendamentoAlunoRepository = agendamentoAlunoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.cancelamentoRepository = cancelamentoRepository;
        this.pacoteService = pacoteService;
        this.agendamentoService = agendamentoService;
        this.rabbitMQ = rabbitMQ;
    }

    // ------------------------------------------------------------------ aulas

    /** Aulas do aluno no intervalo (futuras, realizadas e canceladas). Padrão: 30 dias para trás e para frente. */
    @Transactional(readOnly = true)
    public List<AulaAlunoDTO> aulas(String email, LocalDate de, LocalDate ate) {
        return aulas(email, de, ate, LocalDateTime.now());
    }

    List<AulaAlunoDTO> aulas(String email, LocalDate de, LocalDate ate, LocalDateTime agora) {
        Aluno aluno = alunoDoToken(email);
        LocalDate inicio = de != null ? de : agora.toLocalDate().minusDays(30);
        LocalDate fim = ate != null ? ate : agora.toLocalDate().plusDays(30);
        validarIntervalo(inicio, fim);

        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.plusDays(1).atStartOfDay().minusNanos(1);

        List<AulaAlunoDTO> resultado = new ArrayList<>();
        agendamentoAlunoRepository.buscarDoAlunoNoPeriodo(aluno.getId(), inicioDia, fimDia)
                .forEach(v -> resultado.add(paraDTO(v, agora)));
        cancelamentoRepository.findByAlunoIdAndDataHoraAulaBetweenOrderByDataHoraAulaAsc(aluno.getId(), inicioDia, fimDia)
                .forEach(c -> resultado.add(paraDTO(c)));
        resultado.sort(Comparator.comparing(AulaAlunoDTO::dataHora));
        return resultado;
    }

    @Transactional(readOnly = true)
    public AulaAlunoDTO detalhe(String email, Long agendamentoId) {
        Aluno aluno = alunoDoToken(email);
        return paraDTO(vinculoDoAluno(aluno, agendamentoId), LocalDateTime.now());
    }

    // ------------------------------------------------------ confirmar presença

    /** O aluno avisa que vai comparecer. Só vale para aulas futuras; repetir não faz mal. */
    @Transactional
    public void confirmarPresenca(String email, Long agendamentoId) {
        confirmarPresenca(email, agendamentoId, LocalDateTime.now());
    }

    void confirmarPresenca(String email, Long agendamentoId, LocalDateTime agora) {
        Aluno aluno = alunoDoToken(email);
        AgendamentoAluno vinculo = vinculoDoAluno(aluno, agendamentoId);
        if (!vinculo.getAgendamento().getDataHora().isAfter(agora)) {
            throw new OperacaoInvalidaException("Esta aula já começou ou já aconteceu: não é possível confirmar presença.");
        }
        if (!vinculo.isPresencaConfirmada()) {
            vinculo.confirmarPresenca();
            agendamentoAlunoRepository.save(vinculo);
        }
    }

    // ----------------------------------------------------------- cancelamento

    /**
     * Cancela a participação do aluno na aula.
     * <ul>
     *   <li>24h ou mais de antecedência: a aula volta ao pacote.</li>
     *   <li>Menos de 24h: o cancelamento é permitido, mas a aula continua descontada.</li>
     *   <li>Aula que já começou ou passou: não pode ser cancelada.</li>
     * </ul>
     * Se ele era o único aluno, a aula inteira é removida; senão, o professor é avisado da mudança.
     */
    @Transactional
    public CancelamentoResultadoDTO cancelar(String email, Long agendamentoId) {
        return cancelar(email, agendamentoId, LocalDateTime.now());
    }

    CancelamentoResultadoDTO cancelar(String email, Long agendamentoId, LocalDateTime agora) {
        Aluno aluno = alunoDoToken(email);
        AgendamentoAluno vinculo = vinculoDoAluno(aluno, agendamentoId);
        Agendamento agendamento = vinculo.getAgendamento();

        if (!agendamento.getDataHora().isAfter(agora)) {
            throw new OperacaoInvalidaException("Esta aula já começou ou já aconteceu e não pode ser cancelada.");
        }

        boolean foraDoPrazo = agendamento.getDataHora().isBefore(agora.plus(ANTECEDENCIA_CANCELAMENTO));
        boolean tinhaDebito = vinculo.isCreditoDebitado();
        boolean devolvida = !foraDoPrazo
                && pacoteService.devolver(vinculo, MotivoMovimentoPacote.CANCELAMENTO_ALUNO_COM_DEVOLUCAO);

        cancelamentoRepository.save(new CancelamentoAula(aluno, agendamento, devolvida, foraDoPrazo));
        notificarAlunoCancelamento(aluno, agendamento, foraDoPrazo, tinhaDebito ? devolvida : null);

        agendamento.getAgendamentoAlunos().remove(vinculo);
        if (agendamento.getAgendamentoAlunos().isEmpty()) {
            // era o único aluno: a aula inteira sai (o professor recebe o aviso de cancelamento)
            agendamentoService.excluirAgendamento(agendamento.getId());
        } else {
            agendamentoRepository.save(agendamento);
            notificarProfessorSaidaDoAluno(agendamento);
        }

        logger.info("Aluno {} cancelou a aula {} (foraDoPrazo={}, aulaDevolvida={})",
                aluno.getId(), agendamentoId, foraDoPrazo, devolvida);
        return new CancelamentoResultadoDTO(devolvida, foraDoPrazo, mensagemCancelamento(foraDoPrazo, devolvida));
    }

    private String mensagemCancelamento(boolean foraDoPrazo, boolean devolvida) {
        if (foraDoPrazo) {
            return "Aula cancelada com menos de 24 horas de antecedência: ela foi descontada do seu pacote.";
        }
        return devolvida ? "Aula cancelada. Ela voltou para o seu pacote." : "Aula cancelada.";
    }

    // -------------------------------------------------------------- dashboard

    /** Painel do período (padrão: últimos 30 dias, hoje incluído). */
    @Transactional(readOnly = true)
    public DashboardAlunoDTO dashboard(String email, LocalDate de, LocalDate ate) {
        return dashboard(email, de, ate, LocalDateTime.now());
    }

    DashboardAlunoDTO dashboard(String email, LocalDate de, LocalDate ate, LocalDateTime agora) {
        Aluno aluno = alunoDoToken(email);
        LocalDate fim = ate != null ? ate : agora.toLocalDate();
        LocalDate inicio = de != null ? de : fim.minusDays(29);
        validarIntervalo(inicio, fim);

        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.plusDays(1).atStartOfDay().minusNanos(1);

        List<AgendamentoAluno> realizadas = agendamentoAlunoRepository
                .buscarDoAlunoNoPeriodo(aluno.getId(), inicioDia, fimDia).stream()
                .filter(v -> !v.getAgendamento().getDataHora().isAfter(agora))
                .toList();
        List<CancelamentoAula> cancelamentos = cancelamentoRepository
                .findByAlunoIdAndDataHoraAulaBetweenOrderByDataHoraAulaAsc(aluno.getId(), inicioDia, fimDia);

        Map<String, Long> porEspecialidade = realizadas.stream()
                .collect(Collectors.groupingBy(v -> v.getAgendamento().getEspecialidade().getNome(),
                        LinkedHashMap::new, Collectors.counting()));
        Map<DayOfWeek, Long> porDia = realizadas.stream()
                .collect(Collectors.groupingBy(v -> v.getAgendamento().getDataHora().getDayOfWeek(),
                        () -> new EnumMap<>(DayOfWeek.class), Collectors.counting()));

        List<DashboardAlunoDTO.Contagem> contagemEspecialidades = porEspecialidade.entrySet().stream()
                .map(e -> new DashboardAlunoDTO.Contagem(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(DashboardAlunoDTO.Contagem::quantidade).reversed())
                .toList();
        List<DashboardAlunoDTO.Contagem> contagemDias = new ArrayList<>();
        for (DayOfWeek dia : DayOfWeek.values()) {
            long qtd = porDia.getOrDefault(dia, 0L);
            if (qtd > 0 || dia.getValue() <= DayOfWeek.FRIDAY.getValue()) {
                contagemDias.add(new DashboardAlunoDTO.Contagem(dia.getDisplayName(TextStyle.FULL, PT_BR), qtd));
            }
        }

        String especialidadeTop = contagemEspecialidades.isEmpty() ? null : contagemEspecialidades.get(0).nome();
        String diaTop = porDia.isEmpty() ? null : porDia.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().getDisplayName(TextStyle.FULL, PT_BR))
                .orElse(null);

        List<PacoteResponseDTO> pacotes = pacoteService.listar(aluno.getId());
        int saldoTotal = pacotes.stream().mapToInt(PacoteResponseDTO::saldo).sum();

        AulaAlunoDTO proxima = agendamentoAlunoRepository
                .buscarDoAlunoNoPeriodo(aluno.getId(), agora, agora.plusYears(1)).stream()
                .findFirst().map(v -> paraDTO(v, agora)).orElse(null);

        return new DashboardAlunoDTO(inicio, fim,
                realizadas.size(),
                cancelamentos.size(),
                cancelamentos.stream().filter(CancelamentoAula::isForaDoPrazo).count(),
                agendamentoAlunoRepository.contarFuturas(aluno.getId(), agora),
                especialidadeTop, diaTop, saldoTotal, pacotes, contagemEspecialidades, contagemDias, proxima);
    }

    // ----------------------------------------------------------- perfil/pacote

    @Transactional(readOnly = true)
    public PerfilAlunoDTO perfil(String email) {
        return paraPerfil(alunoDoToken(email));
    }

    @Transactional
    public PerfilAlunoDTO atualizarPerfil(String email, AtualizarPerfilAlunoDTO dto) {
        Aluno aluno = alunoDoToken(email);
        if (dto.telefone() != null) {
            aluno.setTipoContato(dto.telefone().trim());
        }
        if (dto.notificacaoAtiva() != null) {
            aluno.setNotificacaoAtiva(dto.notificacaoAtiva());
        }
        return paraPerfil(alunoRepository.save(aluno));
    }

    @Transactional(readOnly = true)
    public List<PacoteResponseDTO> pacotes(String email) {
        return pacoteService.listar(alunoDoToken(email).getId());
    }

    // ---------------------------------------------------------------- helpers

    private Aluno alunoDoToken(String email) {
        return alunoRepository.findByEmail(email)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado"));
    }

    /** Mesma resposta para "não existe" e "é de outro aluno", para não revelar ids alheios. */
    private AgendamentoAluno vinculoDoAluno(Aluno aluno, Long agendamentoId) {
        return agendamentoAlunoRepository.findByAlunoIdAndAgendamentoId(aluno.getId(), agendamentoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aula não encontrada"));
    }

    private void validarIntervalo(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new OperacaoInvalidaException("A data inicial não pode ser depois da data final.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) > LIMITE_DIAS_CONSULTA) {
            throw new OperacaoInvalidaException("O intervalo máximo de consulta é de " + LIMITE_DIAS_CONSULTA + " dias.");
        }
    }

    private PerfilAlunoDTO paraPerfil(Aluno aluno) {
        return new PerfilAlunoDTO(aluno.getId(), aluno.getNome(), aluno.getEmail(), aluno.getTipoContato(),
                aluno.getStatus(), aluno.getNotificacaoAtiva());
    }

    private AulaAlunoDTO paraDTO(AgendamentoAluno vinculo, LocalDateTime agora) {
        Agendamento ag = vinculo.getAgendamento();
        boolean futura = ag.getDataHora().isAfter(agora);
        boolean semPerda = futura && !ag.getDataHora().isBefore(agora.plus(ANTECEDENCIA_CANCELAMENTO));
        return new AulaAlunoDTO(ag.getId(), ag.getDataHora(), DURACAO_AULA_MINUTOS,
                ag.getEspecialidade().getNome(), ag.getProfessor().getNome(), ag.getProfessor().getId(),
                ag.getProfessor().getFoto(), ag.getSala().getNome(),
                futura ? SituacaoAulaAluno.AGENDADA : SituacaoAulaAluno.REALIZADA,
                vinculo.isPresencaConfirmada(), futura, semPerda, null, null, null);
    }

    private AulaAlunoDTO paraDTO(CancelamentoAula c) {
        return new AulaAlunoDTO(c.getAgendamentoId(), c.getDataHoraAula(), DURACAO_AULA_MINUTOS,
                c.getEspecialidade(), c.getProfessor(), null, null, c.getSala(),
                SituacaoAulaAluno.CANCELADA, false, false, false,
                c.isCreditoDevolvido(), c.isForaDoPrazo(), c.getCanceladoEm());
    }

    // ----------------------------------------------------------------- e-mails

    private void notificarAlunoCancelamento(Aluno aluno, Agendamento agendamento, boolean foraDoPrazo, Boolean devolvida) {
        if (!Boolean.TRUE.equals(aluno.getNotificacaoAtiva()) || aluno.getEmail() == null || aluno.getEmail().isBlank()) {
            return;
        }
        try {
            AulaCanceladaAlunoEmailDTO payload = new AulaCanceladaAlunoEmailDTO();
            payload.setNomeAluno(aluno.getNome());
            payload.setDataHoraAgendamento(agendamento.getDataHora().toString());
            payload.setNomeEspecialidade(agendamento.getEspecialidade().getNome());
            payload.setNomeSala(agendamento.getSala().getNome());
            payload.setCanceladoPeloAluno(true);
            // "fora do prazo" = aula perdida; "no prazo" = devolvida (ou nulo se não havia débito a devolver)
            payload.setAulaDevolvidaAoPacote(foraDoPrazo ? Boolean.FALSE : devolvida);

            EmailRequestDTO email = new EmailRequestDTO();
            email.setTypeEmail(TipoEmail.AULA_CANCELADA_ALUNO);
            email.setDestinatario(aluno.getEmail());
            email.setPayload(payload);
            rabbitMQ.enviarPraFilaDeEmails(email);
        } catch (Exception e) {
            logger.warn("Não foi possível enfileirar o e-mail de cancelamento do aluno {}: {}", aluno.getId(), e.getMessage());
        }
    }

    private void notificarProfessorSaidaDoAluno(Agendamento agendamento) {
        Professor professor = agendamento.getProfessor();
        if (!Boolean.TRUE.equals(professor.getNotificacaoAtiva())) {
            return;
        }
        try {
            AulaAtualizadaEmailDTO payload = new AulaAtualizadaEmailDTO();
            payload.setNomeProfessor(professor.getNome());
            payload.setNomesAlunos(agendamento.getAgendamentoAlunos().stream()
                    .map(aa -> aa.getAluno().getNome()).toList());
            payload.setDestinatario(professor.getEmail());
            payload.setDataHoraAgendamento(agendamento.getDataHora().toString());
            payload.setNomeSala(agendamento.getSala().getNome());
            payload.setNomeEspecialidade(agendamento.getEspecialidade().getNome());

            EmailRequestDTO email = new EmailRequestDTO();
            email.setTypeEmail(TipoEmail.AULA_ATUALIZADA);
            email.setDestinatario(professor.getEmail());
            email.setPayload(payload);
            rabbitMQ.enviarPraFilaDeEmails(email);
        } catch (Exception e) {
            logger.warn("Não foi possível avisar o professor {} da saída do aluno: {}", professor.getId(), e.getMessage());
        }
    }
}
