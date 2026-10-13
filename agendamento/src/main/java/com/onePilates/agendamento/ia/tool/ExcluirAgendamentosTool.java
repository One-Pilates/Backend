package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Exclui aulas FUTURAS por filtros (id, data, professor, sala, aluno).
 * Proteções: só administrador e secretaria; exige ao menos um filtro; limite por operação;
 * sem confirmação explícita escrita pelo usuário, só mostra a prévia e não exclui nada.
 */
@Component
public class ExcluirAgendamentosTool implements FerramentaIA {

    private static final int LIMITE_POR_OPERACAO = 20;
    private static final int DIAS_A_FRENTE = 365;
    private static final Pattern CONFIRMACAO = Pattern.compile(
            "\\bconfirm\\w*|\\bpode (excluir|apagar|cancelar|remover|deletar)\\b|^\\s*sim\\b");

    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;

    public ExcluirAgendamentosTool(AgendamentoRepository agendamentoRepository, AgendamentoService agendamentoService) {
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoService = agendamentoService;
    }

    @Override
    public String nome() { return "excluir_agendamentos"; }

    @Override
    public String descricao() {
        return "Exclui aulas FUTURAS agendadas, filtrando por agendamentoId, data (dataInicio e dataFim), professorId, "
                + "salaId e/ou alunoId (os filtros se somam). Exige ao menos um filtro. Sem confirmado=true a ferramenta "
                + "só mostra a prévia do que seria excluído. Use confirmado=true SOMENTE se o PEDIDO ORIGINAL do usuário "
                + "contiver confirmação explícita (ex.: sim, confirmo, pode excluir). A exclusão envia e-mails de cancelamento "
                + "a professores e alunos e não pode ser desfeita.";
    }

    /** Mesma permissão do DELETE /api/agendamentos/{id}. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "agendamentoId", Map.of("type", "integer"),
                        "dataInicio", Map.of("type", "string", "description", "AAAA-MM-DD"),
                        "dataFim", Map.of("type", "string", "description", "AAAA-MM-DD; se for um dia só, igual à dataInicio"),
                        "professorId", Map.of("type", "integer"),
                        "salaId", Map.of("type", "integer"),
                        "alunoId", Map.of("type", "integer"),
                        "confirmado", Map.of("type", "boolean")),
                "required", List.of());
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long agendamentoId = ArgumentosTool.longOpcional(argumentos, "agendamentoId");
        Long professorId = ArgumentosTool.longOpcional(argumentos, "professorId");
        Long salaId = ArgumentosTool.longOpcional(argumentos, "salaId");
        Long alunoId = ArgumentosTool.longOpcional(argumentos, "alunoId");

        LocalDate dataInicio;
        LocalDate dataFim;
        try {
            dataInicio = argumentos.get("dataInicio") == null ? null : LocalDate.parse(String.valueOf(argumentos.get("dataInicio")));
            dataFim = argumentos.get("dataFim") == null ? dataInicio : LocalDate.parse(String.valueOf(argumentos.get("dataFim")));
        } catch (DateTimeParseException e) {
            return Map.of("erro", "Data inválida, use AAAA-MM-DD");
        }
        if (dataInicio == null && dataFim != null) {
            dataInicio = dataFim;
        }
        if (agendamentoId == null && dataInicio == null && professorId == null && salaId == null && alunoId == null) {
            return Map.of("erro", "Informe ao menos um filtro (agendamentoId, data, professorId, salaId ou alunoId). "
                    + "Não é permitido excluir tudo.");
        }
        if (dataInicio != null && dataFim.isBefore(dataInicio)) {
            return Map.of("erro", "A data de fim não pode ser anterior à de início");
        }

        // só aulas futuras: o histórico de aulas já dadas (presenças, observações) nunca é apagado por aqui
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime de = dataInicio == null ? agora : dataInicio.atStartOfDay();
        if (de.isBefore(agora)) de = agora;
        LocalDateTime ate = dataInicio == null ? agora.plusDays(DIAS_A_FRENTE) : dataFim.plusDays(1).atStartOfDay();

        final Long fAgendamento = agendamentoId, fProfessor = professorId, fSala = salaId, fAluno = alunoId;
        List<Agendamento> alvo = agendamentoRepository.buscarAgendamentosPorIntervalo(de, ate).stream()
                .filter(a -> fAgendamento == null || a.getId().equals(fAgendamento))
                .filter(a -> fProfessor == null || (a.getProfessor() != null && a.getProfessor().getId().equals(fProfessor)))
                .filter(a -> fSala == null || (a.getSala() != null && a.getSala().getId().equals(fSala)))
                .filter(a -> fAluno == null || a.getAgendamentoAlunos().stream().anyMatch(aa -> aa.getAluno().getId().equals(fAluno)))
                .sorted(Comparator.comparing(Agendamento::getDataHora))
                .toList();

        if (alvo.isEmpty()) {
            return Map.of("erro", "Nenhuma aula futura encontrada com esses filtros. Nada foi excluído.");
        }
        if (alvo.size() > LIMITE_POR_OPERACAO) {
            return Map.of("erro", "Esses filtros atingem " + alvo.size() + " aulas, acima do limite de "
                    + LIMITE_POR_OPERACAO + " por pedido. Nada foi excluído; peça ao usuário para restringir os filtros.");
        }

        List<Map<String, Object>> previa = alvo.stream().map(this::resumo).toList();
        boolean confirmadoPeloModelo = Boolean.parseBoolean(String.valueOf(argumentos.get("confirmado")));
        if (!confirmadoPeloModelo || !usuarioConfirmou(usuario.mensagemOriginal())) {
            return Map.of("previa", true, "total", alvo.size(), "aulas", previa,
                    "aviso", "NADA foi excluído. Mostre a lista ao usuário e pergunte se confirma; ele deve responder "
                            + "'sim, confirmo' na próxima mensagem (a conversa anterior fica na memória). "
                            + "Isso envia e-mails de cancelamento e não pode ser desfeito.");
        }

        List<Long> excluidas = new ArrayList<>();
        List<Map<String, Object>> falhas = new ArrayList<>();
        for (Agendamento a : alvo) {
            try {
                agendamentoService.excluirAgendamento(a.getId());
                excluidas.add(a.getId());
            } catch (Exception e) {
                falhas.add(Map.of("id", a.getId(), "erro", String.valueOf(e.getMessage())));
            }
        }
        return Map.of("sucesso", !excluidas.isEmpty(), "excluidas", excluidas, "falhas", falhas, "aulas", previa);
    }

    /** Confirmação escrita pelo próprio usuário no pedido original, verificada em código e não pelo modelo. */
    private boolean usuarioConfirmou(String mensagemOriginal) {
        if (mensagemOriginal == null) return false;
        String texto = Normalizer.normalize(mensagemOriginal, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
        return CONFIRMACAO.matcher(texto).find();
    }

    private Map<String, Object> resumo(Agendamento a) {
        return Map.of(
                "id", a.getId(),
                "horario", a.getDataHora().toString(),
                "professor", a.getProfessor() != null ? a.getProfessor().getNome() : "-",
                "sala", a.getSala() != null ? a.getSala().getNome() : "-",
                "especialidade", a.getEspecialidade() != null ? a.getEspecialidade().getNome() : "-",
                "alunos", a.getAgendamentoAlunos().stream().map(aa -> aa.getAluno().getNome()).toList());
    }
}
