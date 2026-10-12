package com.onePilates.agendamento.administrador.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.administrador.dto.AdministradorDTO;
import com.onePilates.agendamento.administrador.dto.AdministradorResponseDTO;
import com.onePilates.agendamento.administrador.service.AdministradorService;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Administrador;
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

@WebMvcTest(AdministradorController.class)
@Import(TestSecurityConfig.class)
class AdministradorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdministradorService administradorService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private AdministradorDTO dto;
    private AdministradorResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new AdministradorDTO();
        dto.setNome("Admin Teste");
        dto.setEmail("admin@teste.com");
        dto.setCpf("12345678900");
        dto.setIdade(LocalDate.of(1990, 1, 1));
        dto.setSenha("senha123");

        responseDTO = new AdministradorResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNome("Admin Teste");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void criarAdministrador_DeveRetornar200() throws Exception {
        Administrador administrador = new Administrador();
        administrador.setId(1L);
        when(administradorService.criarAdministrador(any(AdministradorDTO.class))).thenReturn(administrador);
        when(administradorService.toResponseDTO(any(Administrador.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/administradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(administradorService).criarAdministrador(any(AdministradorDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void criarAdministrador_DeveRetornar403_QuandoSecretaria() throws Exception {
        mockMvc.perform(post("/api/administradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(administradorService, never()).criarAdministrador(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarAdministradores_DeveRetornar200() throws Exception {
        List<AdministradorResponseDTO> lista = Arrays.asList(responseDTO);
        when(administradorService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/administradores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(administradorService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorId_DeveRetornar200() throws Exception {
        when(administradorService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/administradores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(administradorService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void atualizarAdministradorParcial_DeveRetornar200() throws Exception {
        when(administradorService.atualizarAdministrador(eq(1L), any(AdministradorDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/administradores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(administradorService).atualizarAdministrador(eq(1L), any(AdministradorDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void excluirAdministrador_DeveRetornar204() throws Exception {
        doNothing().when(administradorService).excluirAdministrador(1L);

        mockMvc.perform(delete("/api/administradores/1"))
                .andExpect(status().isNoContent());

        verify(administradorService).excluirAdministrador(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void uploadFoto_DeveRetornar200_QuandoArquivoValido() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(administradorService.salvarFoto(eq(1L), any())).thenReturn("imagens/admin_1_test.jpg");

        mockMvc.perform(multipart("/api/administradores/1/uploadFoto")
                        .file(file))
                .andExpect(status().isOk());

        verify(administradorService).salvarFoto(eq(1L), any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void uploadFoto_DeveRetornar400_QuandoErro() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(administradorService.salvarFoto(eq(1L), any())).thenThrow(new RuntimeException("Erro ao salvar"));

        mockMvc.perform(multipart("/api/administradores/1/uploadFoto")
                        .file(file))
                .andExpect(status().isBadRequest());

        verify(administradorService).salvarFoto(eq(1L), any());
    }
}

