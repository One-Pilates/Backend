package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FerramentaRolesEquipeTest {

    private static final Set<Role> EQUIPE = Set.of(Role.ADMINISTRADOR, Role.SECRETARIA, Role.PROFESSOR);

    @Test
    void listarAgendamentos_DevePermitirSomenteEquipe() {
        assertEquals(EQUIPE, new ListarAgendamentosTool(null).rolesPermitidas());
    }

    @Test
    void listarProximosAgendamentos_DevePermitirSomenteEquipe() {
        assertEquals(EQUIPE, new ListarProximosAgendamentosTool(null).rolesPermitidas());
    }

    @Test
    void listarEspecialidades_DevePermitirSomenteEquipe() {
        assertEquals(EQUIPE, new ListarEspecialidadesTool(null).rolesPermitidas());
    }

    @Test
    void listarProfessoresESalas_DevePermitirSomenteEquipe() {
        assertEquals(EQUIPE, new ListarProfessoresESalasTool(null, null).rolesPermitidas());
    }

    @Test
    void buscarAlunos_DevePermitirSomenteEquipe() {
        assertEquals(EQUIPE, new BuscarAlunosTool(null).rolesPermitidas());
    }

    @Test
    void verObservacoesAluno_DevePermitirSomenteEquipe() {
        assertEquals(EQUIPE, new VerObservacoesAlunoTool(null, null).rolesPermitidas());
    }
}
