package com.onePilates.agendamento.sala.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.sala.dto.SalaDTO;
import com.onePilates.agendamento.sala.dto.SalaResponseDTO;
import com.onePilates.agendamento.sala.service.SalaService;
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

@WebMvcTest(SalaController.class)
@Import(TestSecurityConfig.class)
class SalaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SalaService salaService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private SalaDTO dto;
    private SalaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new SalaDTO();
        dto.setNome("Sala Teste");
        dto.setQuantidadeMaximaAlunos(10);

        responseDTO = new SalaResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNome("Sala Teste");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void criarSala_DeveRetornar200() throws Exception {
        Sala sala = new Sala();
        sala.setId(1L);
        when(salaService.criarSala(any(SalaDTO.class))).thenReturn(sala);
        when(salaService.toResponseDTO(any(Sala.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(salaService).criarSala(any(SalaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void criarSala_DeveRetornar200_QuandoSecretaria() throws Exception {
        Sala sala = new Sala();
        sala.setId(1L);
        when(salaService.criarSala(any(SalaDTO.class))).thenReturn(sala);
        when(salaService.toResponseDTO(any(Sala.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(salaService).criarSala(any(SalaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void criarSala_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(post("/api/salas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(salaService, never()).criarSala(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarSalas_DeveRetornar200() throws Exception {
        List<SalaResponseDTO> lista = Arrays.asList(responseDTO);
        when(salaService.listarTodasDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/salas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(salaService).listarTodasDTO();
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void listarSalas_DeveRetornar200_QuandoProfessor() throws Exception {
        List<SalaResponseDTO> lista = Arrays.asList(responseDTO);
        when(salaService.listarTodasDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/salas"))
                .andExpect(status().isOk());

        verify(salaService).listarTodasDTO();
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorId_DeveRetornar200() throws Exception {
        when(salaService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/salas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(salaService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void atualizarSalaParcial_DeveRetornar200() throws Exception {
        when(salaService.atualizarSala(eq(1L), any(SalaDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/salas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(salaService).atualizarSala(eq(1L), any(SalaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void excluirSala_DeveRetornar204() throws Exception {
        doNothing().when(salaService).excluirSala(1L);

        mockMvc.perform(delete("/api/salas/1"))
                .andExpect(status().isNoContent());

        verify(salaService).excluirSala(1L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void excluirSala_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(delete("/api/salas/1"))
                .andExpect(status().isForbidden());

        verify(salaService, never()).excluirSala(any());
    }
}

