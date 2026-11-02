package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FerramentaRegistryTest {

    @Test
    void definicoes_NaoDeveEntregarToolsDeEquipe_QuandoRoleAluno() {
        List<FerramentaIA> ferramentas = List.of(
                new ListarAgendamentosTool(null),
                new ListarProximosAgendamentosTool(null),
                new ListarEspecialidadesTool(null),
                new ListarProfessoresESalasTool(null, null),
                new BuscarAlunosTool(null),
                new VerObservacoesAlunoTool(null, null),
                new CriarAgendamentoTool(null, null, null),
                new MinhaAgendaTool(mock(com.onePilates.agendamento.aluno.service.PortalAlunoService.class),
                        mock(com.onePilates.agendamento.aluno.service.PacoteAulaService.class)),
                new EspecialidadesEstudioTool(null)
        );

        Set<String> nomes = new FerramentaRegistry(ferramentas).definicoes(Role.ALUNO).stream()
                .map(definicao -> String.valueOf(((Map<?, ?>) definicao.get("function")).get("name")))
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(nomes.contains("minha_agenda"));
        assertTrue(nomes.contains("especialidades_estudio"));
        assertFalse(nomes.contains("listar_agendamentos"));
        assertFalse(nomes.contains("listar_proximos_agendamentos"));
        assertFalse(nomes.contains("listar_especialidades"));
        assertFalse(nomes.contains("listar_professores_e_salas"));
        assertFalse(nomes.contains("buscar_alunos"));
        assertFalse(nomes.contains("ver_observacoes_aluno"));
        assertFalse(nomes.contains("criar_agendamento"));
    }
}
