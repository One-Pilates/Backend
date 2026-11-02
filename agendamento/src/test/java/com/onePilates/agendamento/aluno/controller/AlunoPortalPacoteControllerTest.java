package com.onePilates.agendamento.aluno.controller;

import com.onePilates.agendamento.aluno.dto.PacoteAulaResponseDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.service.PacoteAulaService;
import com.onePilates.agendamento.aluno.service.PortalAlunoService;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Aluno;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlunoPortalController.class)
@Import(TestSecurityConfig.class)
class AlunoPortalPacoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacoteAulaService pacoteAulaService;

    @MockBean
    private PortalAlunoService portalAlunoService;

    @MockBean
    private AlunoRepository alunoRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    @Test
    @WithMockUser(username = "aluno@teste.com", authorities = {"ALUNO"})
    void consultarPacotesDoToken_DeveRetornar200() throws Exception {
        Aluno aluno = new Aluno();
        aluno.setId(5L);
        when(alunoRepository.findByEmail("aluno@teste.com")).thenReturn(Optional.of(aluno));
        when(pacoteAulaService.listar(5L)).thenReturn(List.of(new PacoteAulaResponseDTO()));

        mockMvc.perform(get("/api/aluno/pacotes"))
                .andExpect(status().isOk());
    }
}
