package com.onePilates.agendamento.professor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.dashboard.dto.RespostaDashProfessoraDTO;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.professor.dto.ProfessorDTO;
import com.onePilates.agendamento.professor.dto.ProfessorResponseDTO;
import com.onePilates.agendamento.professor.service.ProfessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfessorController.class)
@Import(TestSecurityConfig.class)
class ProfessorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfessorService professorService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private ProfessorDTO dto;
    private ProfessorResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new ProfessorDTO();
        dto.setNome("Professor Teste");
        dto.setEmail("professor@teste.com");
        dto.setCpf("11144477735"); // CPF válido para testes
        dto.setIdade(LocalDate.of(1990, 1, 1));
        dto.setStatus(true);
        dto.setNotificacaoAtiva(true);
        dto.setCargo("Professor");
        dto.setRole(Role.PROFESSOR);
        dto.setSenha("senha123");
        dto.setEspecialidadeIds(Set.of(1L));

        responseDTO = new ProfessorResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNome("Professor Teste");
        responseDTO.setEmail("professor@teste.com");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void criarProfessor_DeveRetornar201_QuandoAdministrador() throws Exception {
        when(professorService.criarProfessor(any(ProfessorDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(header().exists("Location"));

        verify(professorService).criarProfessor(any(ProfessorDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void criarProfessor_DeveRetornar201_QuandoSecretaria() throws Exception {
        when(professorService.criarProfessor(any(ProfessorDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(professorService).criarProfessor(any(ProfessorDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void criarProfessor_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(post("/api/professores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(professorService, never()).criarProfessor(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarProfessores_DeveRetornar200() throws Exception {
        List<ProfessorResponseDTO> lista = Arrays.asList(responseDTO);
        when(professorService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/professores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(professorService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void listarProfessores_DeveRetornar200_QuandoSecretaria() throws Exception {
        List<ProfessorResponseDTO> lista = Arrays.asList(responseDTO);
        when(professorService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/professores"))
                .andExpect(status().isOk());

        verify(professorService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void listarProfessores_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(get("/api/professores"))
                .andExpect(status().isForbidden());

        verify(professorService, never()).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorId_DeveRetornar200() throws Exception {
        when(professorService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/professores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(professorService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void buscarPorId_DeveRetornar200_QuandoProfessor() throws Exception {
        when(professorService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/professores/1"))
                .andExpect(status().isOk());

        verify(professorService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void atualizarProfessorParcial_DeveRetornar200() throws Exception {
        when(professorService.atualizarProfessor(eq(1L), any(ProfessorDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/professores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(professorService).atualizarProfessor(eq(1L), any(ProfessorDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void atualizarProfessorParcial_DeveRetornar200_QuandoProfessor() throws Exception {
        when(professorService.atualizarProfessor(eq(1L), any(ProfessorDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/professores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(professorService).atualizarProfessor(eq(1L), any(ProfessorDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void excluirProfessor_DeveRetornar204() throws Exception {
        doNothing().when(professorService).excluirProfessor(1L);

        mockMvc.perform(delete("/api/professores/1"))
                .andExpect(status().isNoContent());

        verify(professorService).excluirProfessor(1L);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void excluirProfessor_DeveRetornar403_QuandoSecretaria() throws Exception {
        mockMvc.perform(delete("/api/professores/1"))
                .andExpect(status().isForbidden());

        verify(professorService, never()).excluirProfessor(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorIdDados_DeveRetornar200() throws Exception {
        RespostaDashProfessoraDTO dashDTO = new RespostaDashProfessoraDTO();
        when(professorService.respostaDashProfessora(1L, 30)).thenReturn(dashDTO);

        mockMvc.perform(get("/api/professores/1/30"))
                .andExpect(status().isOk());

        verify(professorService).respostaDashProfessora(1L, 30);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void buscarPorIdDados_DeveRetornar200_QuandoProfessor() throws Exception {
        RespostaDashProfessoraDTO dashDTO = new RespostaDashProfessoraDTO();
        when(professorService.respostaDashProfessora(1L, 30)).thenReturn(dashDTO);

        mockMvc.perform(get("/api/professores/1/30"))
                .andExpect(status().isOk());

        verify(professorService).respostaDashProfessora(1L, 30);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorIdDados_DeveRetornar400_QuandoIdInvalido() throws Exception {
        mockMvc.perform(get("/api/professores/0/30"))
                .andExpect(status().isBadRequest());

        verify(professorService, never()).respostaDashProfessora(any(), any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorIdDados_DeveRetornar400_QuandoDiasInvalidos() throws Exception {
        mockMvc.perform(get("/api/professores/1/400"))
                .andExpect(status().isBadRequest());

        verify(professorService, never()).respostaDashProfessora(any(), any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void uploadFoto_DeveRetornar200_QuandoArquivoValido() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(professorService.salvarFoto(eq(1L), any())).thenReturn("imagens/professor_1_test.jpg");

        mockMvc.perform(multipart("/api/professores/1/uploadFoto")
                        .file(file))
                .andExpect(status().isOk());

        verify(professorService).salvarFoto(eq(1L), any());
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void uploadFoto_DeveRetornar200_QuandoProfessor() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(professorService.salvarFoto(eq(1L), any())).thenReturn("imagens/professor_1_test.jpg");

        mockMvc.perform(multipart("/api/professores/1/uploadFoto")
                        .file(file))
                .andExpect(status().isOk());

        verify(professorService).salvarFoto(eq(1L), any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void uploadFoto_DeveRetornar400_QuandoArquivoVazio() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/professores/1/uploadFoto")
                        .file(file))
                .andExpect(status().isBadRequest());

        verify(professorService, never()).salvarFoto(any(), any());
    }
}

