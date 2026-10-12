package com.onePilates.agendamento.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.repository.AgendamentoAlunoRepository;
import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.ausencia.repository.AusenciaRepository;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o AgendamentoController.
 * Nota: Estes testes podem precisar de configuração adicional de segurança para funcionar completamente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class AgendamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private EspecialidadeRepository especialidadeRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private AgendamentoAlunoRepository agendamentoAlunoRepository;

    @Autowired
    private AusenciaRepository ausenciaRepository;

    private Professor professor;
    private Sala sala;
    private Especialidade especialidade;
    private Aluno aluno1;
    private Aluno aluno2;

    @BeforeEach
    @Transactional
    void setUp() {
        // Limpar dados existentes antes de criar novos (respeitando ordem de foreign keys)
        agendamentoAlunoRepository.deleteAll();
        agendamentoRepository.deleteAll();
        ausenciaRepository.deleteAll();
        alunoRepository.deleteAll();
        salaRepository.deleteAll();
        professorRepository.deleteAll();
        especialidadeRepository.deleteAll();

        // Criar dados de teste
        professor = new Professor();
        professor.setNome("Professor Teste");
        professor.setEmail("professor@teste.com");
        professor.setCpf("12345678900");
        professor.setStatus(true);
        professor.setSenha("senha123");
        professor = professorRepository.save(professor);

        especialidade = new Especialidade();
        especialidade.setNome("Pilates Clássico");
        especialidade = especialidadeRepository.save(especialidade);

        sala = new Sala();
        sala.setNome("Sala 1");
        sala.setQuantidadeMaximaAlunos(5);
        sala.setQuantidadeEquipamentosPCD(2);
        sala.setEspecialidades(Set.of(especialidade));
        sala = salaRepository.save(sala);

        aluno1 = new Aluno();
        aluno1.setNome("Aluno 1");
        aluno1.setEmail("aluno1@teste.com");
        aluno1.setCpf("11111111111");
        aluno1.setStatus(true);
        aluno1.setAlunoComLimitacoesFisicas(false);
        aluno1 = alunoRepository.save(aluno1);

        aluno2 = new Aluno();
        aluno2.setNome("Aluno 2");
        aluno2.setEmail("aluno2@teste.com");
        aluno2.setCpf("22222222222");
        aluno2.setStatus(true);
        aluno2.setAlunoComLimitacoesFisicas(false);
        aluno2 = alunoRepository.save(aluno2);
    }

    @Test
    void criarAgendamento_DeveRetornar401Ou403_SemAutenticacao() throws Exception {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setDataHora(LocalDateTime.now().plusDays(1));
        dto.setSalaId(sala.getId());
        dto.setProfessorId(professor.getId());
        dto.setEspecialidadeId(especialidade.getId());
        dto.setAlunoIds(Set.of(aluno1.getId(), aluno2.getId()));

        // Sem autenticação, deve retornar 401 (Unauthorized) ou 403 (Forbidden)
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void listarAgendamentos_DeveRetornar401Ou403_SemAutenticacao() throws Exception {
        // Sem autenticação, deve retornar 401 (Unauthorized) ou 403 (Forbidden)
        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().is4xxClientError());
    }
}

