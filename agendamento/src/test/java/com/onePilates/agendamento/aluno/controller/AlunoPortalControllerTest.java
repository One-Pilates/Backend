package com.onePilates.agendamento.aluno.controller;

import com.onePilates.agendamento.aluno.dto.RespostaDashAlunoDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.service.PacoteAulaService;
import com.onePilates.agendamento.aluno.service.PortalAlunoService;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.exception.AulaAindaNaoRealizadaException;
import com.onePilates.agendamento.exception.AulaJaRealizadaException;
import com.onePilates.agendamento.exception.CancelamentoForaDoPrazoException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.FeedbackJaExistenteException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Aluno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlunoPortalController.class)
@Import(TestSecurityConfig.class)
class AlunoPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortalAlunoService portalAlunoService;

    @MockBean
    private PacoteAulaService pacoteAulaService;

    @MockBean
    private AlunoRepository alunoRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    @BeforeEach
    void setUp() {
        Aluno aluno = new Aluno();
        aluno.setId(5L);
        when(alunoRepository.findByEmail("aluno@teste.com")).thenReturn(Optional.of(aluno));
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void agenda_DeveRetornar200_QuandoAluno() throws Exception {
        when(portalAlunoService.listarAgenda(eq(5L), isNull(), isNull())).thenReturn(List.of());
        mockMvc.perform(get("/api/aluno/agenda")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PROFESSOR")
    void agenda_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(get("/api/aluno/agenda")).andExpect(status().isForbidden());
    }

    @Test
    void agenda_DeveRetornar401_QuandoAnonimo() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/aluno/agenda")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void confirmarPresenca_DeveRetornar200_QuandoAluno() throws Exception {
        mockMvc.perform(patch("/api/aluno/agenda/1/presenca")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void confirmarPresenca_DeveRetornar400_QuandoAulaJaRealizada() throws Exception {
        doThrow(new AulaJaRealizadaException()).when(portalAlunoService).confirmarPresenca(5L, 1L);
        mockMvc.perform(patch("/api/aluno/agenda/1/presenca"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoErro").value("AULA_JA_REALIZADA"));
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void confirmarPresenca_DeveRetornar404_QuandoAulaNaoEhDoAluno() throws Exception {
        doThrow(new EntidadeNaoEncontradaException("Aula não encontrada")).when(portalAlunoService).confirmarPresenca(5L, 1L);
        mockMvc.perform(patch("/api/aluno/agenda/1/presenca")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void cancelar_DeveRetornar204_QuandoAluno() throws Exception {
        mockMvc.perform(delete("/api/aluno/agenda/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void cancelar_DeveRetornar400_QuandoForaDoPrazo() throws Exception {
        doThrow(new CancelamentoForaDoPrazoException()).when(portalAlunoService).cancelar(5L, 1L);
        mockMvc.perform(delete("/api/aluno/agenda/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoErro").value("CANCELAMENTO_FORA_DO_PRAZO"));
    }

    @Test
    @WithMockUser(authorities = "SECRETARIA")
    void cancelar_DeveRetornar403_QuandoSecretaria() throws Exception {
        mockMvc.perform(delete("/api/aluno/agenda/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void criarFeedback_DeveRetornar201_QuandoAluno() throws Exception {
        mockMvc.perform(post("/api/aluno/agenda/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":5}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void criarFeedback_DeveRetornar400_QuandoAulaAindaNaoRealizada() throws Exception {
        doThrow(new AulaAindaNaoRealizadaException()).when(portalAlunoService).criarFeedback(eq(5L), eq(1L), eq(5), isNull());
        mockMvc.perform(post("/api/aluno/agenda/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void criarFeedback_DeveRetornar409_QuandoJaExiste() throws Exception {
        doThrow(new FeedbackJaExistenteException()).when(portalAlunoService).criarFeedback(eq(5L), eq(1L), eq(5), isNull());
        mockMvc.perform(post("/api/aluno/agenda/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":5}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void atualizarFeedback_DeveRetornar200() throws Exception {
        mockMvc.perform(patch("/api/aluno/agenda/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":4}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void atualizarFeedback_DeveRetornar404() throws Exception {
        doThrow(new EntidadeNaoEncontradaException("Feedback não encontrado"))
                .when(portalAlunoService).atualizarFeedback(eq(5L), eq(1L), eq(4), isNull());
        mockMvc.perform(patch("/api/aluno/agenda/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":4}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void perfil_DeveRetornar200_QuandoAluno() throws Exception {
        mockMvc.perform(get("/api/aluno/perfil")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void atualizarPerfil_DeveRetornar200() throws Exception {
        mockMvc.perform(patch("/api/aluno/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notificacaoAtiva\":true,\"tipoContato\":\"whatsapp\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void uploadFoto_DeveRetornar200_QuandoAluno() throws Exception {
        when(portalAlunoService.uploadFoto(eq(5L), any())).thenReturn("alunos/5/foto.jpg");
        MockMultipartFile foto = new MockMultipartFile("foto", "a.jpg", "image/jpeg", new byte[]{1});
        mockMvc.perform(multipart("/api/aluno/perfil/foto").file(foto))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("alunos/5/foto.jpg")));
    }

    @Test
    @WithMockUser(authorities = "PROFESSOR")
    void perfil_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(get("/api/aluno/perfil")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void dashboard_DeveRetornar200_QuandoAluno() throws Exception {
        when(portalAlunoService.dashboard(5L, 30)).thenReturn(new RespostaDashAlunoDTO());
        mockMvc.perform(get("/api/aluno/dashboard/30")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = "ALUNO")
    void dashboard_DeveRetornar400_QuandoPeriodoInvalido() throws Exception {
        doThrow(new OperacaoInvalidaException("Período deve estar entre 1 e 365 dias"))
                .when(portalAlunoService).dashboard(5L, 0);
        mockMvc.perform(get("/api/aluno/dashboard/0")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "PROFESSOR")
    void dashboard_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(get("/api/aluno/dashboard/30")).andExpect(status().isForbidden());
    }
}
