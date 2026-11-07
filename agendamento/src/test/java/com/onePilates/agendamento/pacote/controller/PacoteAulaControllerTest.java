package com.onePilates.agendamento.pacote.controller;

import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;
import com.onePilates.agendamento.pacote.dto.VerificacaoPacoteDTO;
import com.onePilates.agendamento.pacote.service.PacoteAulaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Só secretaria e administrador consultam e alteram o pacote de aulas. */
@WebMvcTest(PacoteAulaController.class)
@Import(TestSecurityConfig.class)
class PacoteAulaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacoteAulaService pacoteService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private FuncionarioRepository funcionarioRepository;

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void secretaria_listaOPacoteDoAluno() throws Exception {
        when(pacoteService.listar(1L)).thenReturn(List.of(new PacoteResponseDTO(10L, "Pilates", 8)));

        mockMvc.perform(get("/api/alunos/1/pacotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].saldo").value(8));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void secretaria_defineOSaldo() throws Exception {
        when(pacoteService.definirSaldo(1L, 10L, 12)).thenReturn(new PacoteResponseDTO(10L, "Pilates", 12));

        mockMvc.perform(put("/api/alunos/1/pacotes/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aulas\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(12));

        verify(pacoteService).definirSaldo(1L, 10L, 12);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void administrador_defineOSaldo() throws Exception {
        when(pacoteService.definirSaldo(1L, 10L, 0)).thenReturn(new PacoteResponseDTO(10L, "Pilates", 0));

        mockMvc.perform(put("/api/alunos/1/pacotes/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aulas\":0}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ALUNO"})
    void aluno_naoAlteraONemVeOPacotePorAquiNemOHistorico() throws Exception {
        mockMvc.perform(put("/api/alunos/1/pacotes/10")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aulas\":99}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/alunos/1/pacotes")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/alunos/1/pacotes/historico")).andExpect(status().isForbidden());

        verifyNoInteractions(pacoteService);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void professor_naoAlteraNemVeOPacote() throws Exception {
        mockMvc.perform(put("/api/alunos/1/pacotes/10")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aulas\":99}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/alunos/1/pacotes")).andExpect(status().isForbidden());

        verifyNoInteractions(pacoteService);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void verificarPacote_avisaAntesDeAgendar() throws Exception {
        when(pacoteService.verificar(any(), any())).thenReturn(List.of(
                new VerificacaoPacoteDTO(1L, "Ana", 0, true, "Ana não tem aulas disponíveis no pacote de Pilates.")));

        mockMvc.perform(get("/api/agendamentos/verificar-pacote")
                        .param("alunoIds", "1,2").param("especialidadeId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].excedeUmPacote").value(true))
                .andExpect(jsonPath("$[0].mensagem").value("Ana não tem aulas disponíveis no pacote de Pilates."));

        verify(pacoteService).verificar(List.of(1L, 2L), 10L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void verificarPacote_professorNaoUsa() throws Exception {
        mockMvc.perform(get("/api/agendamentos/verificar-pacote")
                        .param("alunoIds", "1").param("especialidadeId", "10"))
                .andExpect(status().isForbidden());
    }
}
