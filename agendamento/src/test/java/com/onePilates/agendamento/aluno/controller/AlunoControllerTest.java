package com.onePilates.agendamento.aluno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.aluno.dto.AlunoDTO;
import com.onePilates.agendamento.aluno.dto.AlunoResponseDTO;
import com.onePilates.agendamento.aluno.service.AlunoService;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Aluno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlunoController.class)
@Import(TestSecurityConfig.class)
class AlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlunoService alunoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private AlunoDTO dto;
    private AlunoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new AlunoDTO();
        dto.setNome("Aluno Teste");
        dto.setEmail("aluno@teste.com");
        dto.setCpf("12345678900");

        responseDTO = new AlunoResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNome("Aluno Teste");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void criarAluno_DeveRetornar200() throws Exception {
        Aluno aluno = new Aluno();
        aluno.setId(1L);
        when(alunoService.criarAluno(any(AlunoDTO.class))).thenReturn(aluno);
        when(alunoService.toResponseDTO(any(Aluno.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(alunoService).criarAluno(any(AlunoDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void criarAluno_DeveRetornar200_QuandoSecretaria() throws Exception {
        Aluno aluno = new Aluno();
        aluno.setId(1L);
        when(alunoService.criarAluno(any(AlunoDTO.class))).thenReturn(aluno);
        when(alunoService.toResponseDTO(any(Aluno.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(alunoService).criarAluno(any(AlunoDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void criarAluno_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(post("/api/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(alunoService, never()).criarAluno(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarAlunos_DeveRetornar200() throws Exception {
        List<AlunoResponseDTO> lista = Arrays.asList(responseDTO);
        when(alunoService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/alunos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(alunoService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void listarAlunos_DeveRetornar200_QuandoProfessor() throws Exception {
        List<AlunoResponseDTO> lista = Arrays.asList(responseDTO);
        when(alunoService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/alunos"))
                .andExpect(status().isOk());

        verify(alunoService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorId_DeveRetornar200() throws Exception {
        when(alunoService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/alunos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(alunoService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void buscarPorId_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(get("/api/alunos/1"))
                .andExpect(status().isForbidden());

        verify(alunoService, never()).buscarPorIdDTO(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void atualizarAlunoParcial_DeveRetornar200() throws Exception {
        when(alunoService.atualizarAluno(eq(1L), any(AlunoDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/alunos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(alunoService).atualizarAluno(eq(1L), any(AlunoDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void excluirAluno_DeveRetornar204() throws Exception {
        doNothing().when(alunoService).excluirAluno(1L);

        mockMvc.perform(delete("/api/alunos/1"))
                .andExpect(status().isNoContent());

        verify(alunoService).excluirAluno(1L);
    }
}

