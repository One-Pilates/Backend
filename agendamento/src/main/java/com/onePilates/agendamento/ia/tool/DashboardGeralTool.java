package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.dashboard.dto.ResponsDashSecretariaAdmDTO;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.secretaria.service.SecretariaService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Dashboard geral do estúdio. Mesmos perfis do endpoint GET /secretaria/qtdUltimosDias/{dias}. */
@Component
public class DashboardGeralTool implements FerramentaIA {

    private final SecretariaService secretariaService;

    public DashboardGeralTool(SecretariaService secretariaService) {
        this.secretariaService = secretariaService;
    }

    @Override
    public String nome() { return "dashboard_geral"; }

    @Override
    public String descricao() {
        return "Indicadores do dashboard geral do estúdio nos últimos N dias (padrão 30, máximo 365): aulas por dia da "
                + "semana, quantidade de sessões por professor e total de alunos atendidos. Use para perguntas sobre "
                + "desempenho, movimento, professores mais ativos ou dias mais cheios.";
    }

    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of("dias", Map.of("type", "integer", "description", "Últimos N dias (1 a 365)")),
                "required", List.of());
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long informado = ArgumentosTool.longOpcional(argumentos, "dias");
        int dias = (int) Math.max(1, Math.min(informado == null ? 30 : informado, 365));
        ResponsDashSecretariaAdmDTO dash = secretariaService.respostaDashSecretariaAdm(dias);
        return Map.of(
                "periodo", "últimos " + dias + " dias",
                "aulasPorDiaDaSemana", dash.getAgendamentosPorDias(),
                "sessoesPorProfessor", dash.getQtdSessoesPorProfessor(),
                "alunosAtendidos", dash.getQtdDeAlunosAtendidos());
    }
}
