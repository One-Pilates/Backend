package com.onePilates.agendamento.aluno.portal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.aluno.portal.dto.AtualizarPerfilAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.CancelamentoResultadoDTO;
import com.onePilates.agendamento.aluno.portal.dto.PerfilAlunoDTO;
import com.onePilates.agendamento.aluno.portal.service.PortalAlunoService;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlunoPortalController.class)
@Import(TestSecurityConfig.class)
class AlunoPortalControllerTest {

    private static final String EMAIL = "ana@teste.com";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PortalAlunoService portalService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private FuncionarioRepository funcionarioRepository;

    // -------------------------------------------------------------- permissões

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ALUNO"})
    void aluno_acessaAsPropriasAulas() throws Exception {
        when(portalService.aulas(eq(EMAIL), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/aluno/aulas")).andExpect(status().isOk());

        verify(portalService).aulas(EMAIL, null, null);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void administrador_naoEntraNoPortalDoAluno() throws Exception {
        mockMvc.perform(get("/api/aluno/aulas")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/aluno/dashboard")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/aluno/perfil")).andExpect(status().isForbidden());

        verifyNoInteractions(portalService);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void secretaria_naoEntraNoPortalDoAluno() throws Exception {
        mockMvc.perform(get("/api/aluno/aulas")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/aluno/aulas/1")).andExpect(status().isForbidden());

        verifyNoInteractions(portalService);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void professor_naoEntraNoPortalDoAluno() throws Exception {
        mockMvc.perform(get("/api/aluno/pacotes")).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/aluno/aulas/1/presenca")).andExpect(status().isForbidden());

        verifyNoInteractions(portalService);
    }

    @Test
    void semLogin_naoEntra() throws Exception {
        mockMvc.perform(get("/api/aluno/aulas")).andExpect(status().is4xxClientError());

        verifyNoInteractions(portalService);
    }

    // ------------------------------------------------------------------- rotas

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ALUNO"})
    void aulas_repassaOIntervaloInformado() throws Exception {
        when(portalService.aulas(eq(EMAIL), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/aluno/aulas").param("de", "2026-11-01").param("ate", "2026-11-30"))
                .andExpect(status().isOk());

        verify(portalService).aulas(EMAIL, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ALUNO"})
    void aulas_dataEmFormatoInvalidoEhRecusada() throws Exception {
        mockMvc.perform(get("/api/aluno/aulas").param("de", "01/11/2026")).andExpect(status().is4xxClientError());

        verify(portalService, never()).aulas(any(), any(), any());
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ALUNO"})
    void cancelar_devolveOResultadoDoCancelamento() throws Exception {
        when(portalService.cancelar(EMAIL, 100L)).thenReturn(
                new CancelamentoResultadoDTO(false, true, "Aula cancelada com menos de 24 horas de antecedência."));

        mockMvc.perform(delete("/api/aluno/aulas/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aulaDevolvida").value(false))
                .andExpect(jsonPath("$.foraDoPrazo").value(true))
                .andExpect(jsonPath("$.mensagem").value("Aula cancelada com menos de 24 horas de antecedência."));
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ALUNO"})
    void cancelar_regraDeNegocioViraErro400() throws Exception {
        when(portalService.cancelar(EMAIL, 100L)).thenThrow(new OperacaoInvalidaException("Esta aula já aconteceu."));

        mockMvc.perform(delete("/api/aluno/aulas/100")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ALUNO"})
    void confirmarPresenca_responde204() throws Exception {
        mockMvc.perform(patch("/api/aluno/aulas/100/presenca")).andExpect(status().isNoContent());

        verify(portalService).confirmarPresenca(EMAIL, 100L);
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ALUNO"})
    void pacotes_listaOSaldoDoProprioAluno() throws Exception {
        when(portalService.pacotes(EMAIL)).thenReturn(List.of(new PacoteResponseDTO(10L, "Pilates", 7)));

        mockMvc.perform(get("/api/aluno/pacotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especialidade").value("Pilates"))
                .andExpect(jsonPath("$[0].saldo").value(7));
    }

    @Test
    @WithMockUser(username = EMAIL, authorities = {"ALUNO"})
    void perfil_atualizaSoTelefoneENotificacoes() throws Exception {
        when(portalService.atualizarPerfil(eq(EMAIL), any(AtualizarPerfilAlunoDTO.class)))
                .thenReturn(new PerfilAlunoDTO(1L, "Ana", EMAIL, "11 98765-4321", true, false));

        mockMvc.perform(patch("/api/aluno/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefone\":\"11 98765-4321\",\"notificacaoAtiva\":false,\"nome\":\"Hacker\",\"cpf\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.telefone").value("11 98765-4321"));

        verify(portalService).atualizarPerfil(EMAIL, new AtualizarPerfilAlunoDTO("11 98765-4321", false));
    }
}
