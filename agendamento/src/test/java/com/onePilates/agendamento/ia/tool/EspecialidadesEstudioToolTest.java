package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EspecialidadesEstudioToolTest {

    @Test
    void especialidadesEstudio_DeveListarNomeProfessorESala_SemIdDeAgendamento() {
        Professor professor = new Professor();
        professor.setNome("Professora");
        Sala sala = new Sala();
        sala.setNome("Sala 1");
        Especialidade especialidade = new Especialidade();
        especialidade.setId(77L);
        especialidade.setNome("Pilates");
        especialidade.setProfessores(Set.of(professor));
        especialidade.setSalas(Set.of(sala));

        EspecialidadeRepository repository = mock(EspecialidadeRepository.class);
        when(repository.findAll()).thenReturn(List.of(especialidade));

        Object resposta = new EspecialidadesEstudioTool(repository)
                .executar(Map.of(), new ContextoUsuario(5L, "Ana", Role.ALUNO, "especialidades"));

        String texto = resposta.toString();
        assertTrue(texto.contains("Pilates"));
        assertTrue(texto.contains("Professora"));
        assertTrue(texto.contains("Sala 1"));
        assertFalse(texto.contains("agendamento"));
        assertFalse(texto.contains("77"));
        assertFalse(texto.contains("aluno"));
    }
}
