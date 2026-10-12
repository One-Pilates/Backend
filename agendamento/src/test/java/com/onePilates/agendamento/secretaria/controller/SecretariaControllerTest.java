package com.onePilates.agendamento.secretaria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.dashboard.dto.ResponsDashSecretariaAdmDTO;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Secretaria;
import com.onePilates.agendamento.secretaria.dto.SecretariaDTO;
import com.onePilates.agendamento.secretaria.dto.SecretariaResponseDTO;
import com.onePilates.agendamento.secretaria.service.SecretariaService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SecretariaController.class)
@Import(TestSecurityConfig.class)
class SecretariaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SecretariaService secretariaService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private SecretariaDTO dto;
    private SecretariaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new SecretariaDTO();
        dto.setNome("Secretaria Teste");
        dto.setEmail("secretaria@teste.com");
        dto.setCpf("11144477735"); // CPF válido para testes
        dto.setIdade(LocalDate.of(1990, 1, 1));
        dto.setStatus(true);
        dto.setNotificacaoAtiva(true);
        dto.setCargo("Secretária");
        dto.setSenha("senha123");

        responseDTO = new SecretariaResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNome("Secretaria Teste");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void criarSecretaria_DeveRetornar200() throws Exception {
        Secretaria secretaria = new Secretaria();
        secretaria.setId(1L);
        when(secretariaService.criarSecretaria(any(SecretariaDTO.class))).thenReturn(secretaria);
        when(secretariaService.toResponseDTO(any(Secretaria.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/secretarias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(secretariaService).criarSecretaria(any(SecretariaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void criarSecretaria_DeveRetornar403_QuandoSecretaria() throws Exception {
        mockMvc.perform(post("/api/secretarias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(secretariaService, never()).criarSecretaria(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarSecretarias_DeveRetornar200() throws Exception {
        List<SecretariaResponseDTO> lista = Arrays.asList(responseDTO);
        when(secretariaService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/secretarias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(secretariaService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorId_DeveRetornar200() throws Exception {
        when(secretariaService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/secretarias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(secretariaService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void buscarPorId_DeveRetornar200_QuandoSecretaria() throws Exception {
        when(secretariaService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/secretarias/1"))
                .andExpect(status().isOk());

        verify(secretariaService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void atualizarSecretariaParcial_DeveRetornar200() throws Exception {
        when(secretariaService.atualizarSecretaria(eq(1L), any(SecretariaDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/secretarias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(secretariaService).atualizarSecretaria(eq(1L), any(SecretariaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void atualizarSecretariaParcial_DeveRetornar200_QuandoSecretaria() throws Exception {
        when(secretariaService.atualizarSecretaria(eq(1L), any(SecretariaDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/secretarias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(secretariaService).atualizarSecretaria(eq(1L), any(SecretariaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void excluirSecretaria_DeveRetornar204() throws Exception {
        doNothing().when(secretariaService).excluirSecretaria(1L);

        mockMvc.perform(delete("/api/secretarias/1"))
                .andExpect(status().isNoContent());

        verify(secretariaService).excluirSecretaria(1L);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void excluirSecretaria_DeveRetornar403_QuandoSecretaria() throws Exception {
        mockMvc.perform(delete("/api/secretarias/1"))
                .andExpect(status().isForbidden());

        verify(secretariaService, never()).excluirSecretaria(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void uploadFoto_DeveRetornar200_QuandoArquivoValido() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(secretariaService.salvarFoto(eq(1L), any())).thenReturn("imagens/secretaria_1_test.jpg");

        mockMvc.perform(multipart("/api/secretarias/1/uploadFoto")
                        .file(file))
                .andExpect(status().isOk());

        verify(secretariaService).salvarFoto(eq(1L), any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void uploadFoto_DeveRetornar400_QuandoErro() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(secretariaService.salvarFoto(eq(1L), any())).thenThrow(new RuntimeException("Erro ao salvar"));

        mockMvc.perform(multipart("/api/secretarias/1/uploadFoto")
                        .file(file))
                .andExpect(status().isBadRequest());

        verify(secretariaService).salvarFoto(eq(1L), any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorIdDados_DeveRetornar200() throws Exception {
        ResponsDashSecretariaAdmDTO dashDTO = new ResponsDashSecretariaAdmDTO();
        when(secretariaService.respostaDashSecretariaAdm(30)).thenReturn(dashDTO);

        mockMvc.perform(get("/api/secretarias/qtdUltimosDias/30"))
                .andExpect(status().isOk());

        verify(secretariaService).respostaDashSecretariaAdm(30);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorIdDados_DeveRetornar400_QuandoDiasInvalidos() throws Exception {
        mockMvc.perform(get("/api/secretarias/qtdUltimosDias/400"))
                .andExpect(status().isBadRequest());

        verify(secretariaService, never()).respostaDashSecretariaAdm(any());
    }
}

