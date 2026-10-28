package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.model.Agendamento;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class ListarProximosAgendamentosTool implements FerramentaIA {

    private static final int DIAS_A_FRENTE = 180;
    private static final int LIMITE_PADRAO = 10;
    private static final int LIMITE_MAXIMO = 30;

    private final AgendamentoRepository agendamentoRepository;

    public ListarProximosAgendamentosTool(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public String nome() { return "listar_proximos_agendamentos"; }

    @Override
    public String descricao() {
        return "Lista as próximas aulas (a partir de agora) em que um aluno vai participar OU que um professor vai dar, "
                + "em ordem de data. Informe alunoId OU professorId (um só). Descubra o alunoId com buscar_alunos e o "
                + "professorId com listar_professores_e_salas.";
    }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "alunoId", Map.of("type", "integer"),
                        "professorId", Map.of("type", "integer"),
                        "limite", Map.of("type", "integer", "description", "Quantidade máxima de aulas (padrão 10, máximo 30)")),
                "required", List.of());
    }

    /** Lê coleções lazy (alunos da aula): sem requisição HTTP não há sessão aberta, então precisa de transação. */
    @Override
    @Transactional(readOnly = true)
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long alunoId = ArgumentosTool.longOpcional(argumentos, "alunoId");
        Long professorId = ArgumentosTool.longOpcional(argumentos, "professorId");
        if ((alunoId == null) == (professorId == null)) {
            return Map.of("erro", "Informe alunoId ou professorId (somente um dos dois)");
        }
        Long limiteInformado = ArgumentosTool.longOpcional(argumentos, "limite");
        int limite = limiteInformado == null ? LIMITE_PADRAO : (int) Math.max(1, Math.min(limiteInformado, LIMITE_MAXIMO));

        LocalDateTime agora = LocalDateTime.now();
        List<Agendamento> todos = agendamentoRepository
                .buscarAgendamentosPorIntervalo(agora, agora.plusDays(DIAS_A_FRENTE)).stream()
                .filter(a -> alunoId != null
                        ? a.getAgendamentoAlunos().stream().anyMatch(aa -> aa.getAluno().getId().equals(alunoId))
                        : a.getProfessor() != null && a.getProfessor().getId().equals(professorId))
                .sorted(Comparator.comparing(Agendamento::getDataHora))
                .toList();

        List<Map<String, Object>> aulas = todos.stream().limit(limite)
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "horario", a.getDataHora().toString(),
                        "professor", a.getProfessor() != null ? ArgumentosTool.texto(a.getProfessor().getNome()) : "-",
                        "sala", a.getSala() != null ? ArgumentosTool.texto(a.getSala().getNome()) : "-",
                        "especialidade", a.getEspecialidade() != null ? ArgumentosTool.texto(a.getEspecialidade().getNome()) : "-",
                        "alunos", a.getAgendamentoAlunos().stream().map(aa -> ArgumentosTool.texto(aa.getAluno().getNome())).toList()))
                .toList();
        return Map.of("proximasAulas", aulas, "totalNosProximos" + DIAS_A_FRENTE + "Dias", todos.size());
    }
}
