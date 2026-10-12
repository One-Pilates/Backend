package com.onePilates.agendamento.ausencia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.ausencia.dto.AusenciaDTO;
import com.onePilates.agendamento.ausencia.dto.AusenciaResponseDTO;
import com.onePilates.agendamento.ausencia.service.AusenciaService;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.enums.DiaSemana;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AusenciaController.class)
@Import(TestSecurityConfig.class)
class AusenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AusenciaService ausenciaService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private AusenciaDTO dto;
    private AusenciaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new AusenciaDTO();
        dto.setProfessorId(1L);
        dto.setDataInicio(LocalDateTime.now().plusDays(1));
        dto.setDataFim(LocalDateTime.now().plusDays(5));
        dto.setDiaSemanaInicio(DiaSemana.SEGUNDA);
        dto.setDiaSemanaFim(DiaSemana.SEXTA);
        dto.setMotivo("Férias");

        responseDTO = new AusenciaResponseDTO();
        responseDTO.setId(1);
        responseDTO.setNomeProfessor("Professor Teste");
        responseDTO.setMotivo("Férias");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void registrar_DeveRetornar201() throws Exception {
        when(ausenciaService.registrarAusencia(any(AusenciaDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/ausencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(ausenciaService).registrarAusencia(any(AusenciaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void registrar_DeveRetornar201_QuandoProfessor() throws Exception {
        when(ausenciaService.registrarAusencia(any(AusenciaDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/ausencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(ausenciaService).registrarAusencia(any(AusenciaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarPorProfessor_DeveRetornar200() throws Exception {
        List<AusenciaResponseDTO> lista = Arrays.asList(responseDTO);
        when(ausenciaService.listarPorProfessor(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/ausencias/professor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(ausenciaService).listarPorProfessor(1L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void listarPorProfessor_DeveRetornar200_QuandoProfessor() throws Exception {
        List<AusenciaResponseDTO> lista = Arrays.asList(responseDTO);
        when(ausenciaService.listarPorProfessor(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/ausencias/professor/1"))
                .andExpect(status().isOk());

        verify(ausenciaService).listarPorProfessor(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void atualizarAusenciaParcial_DeveRetornar200() throws Exception {
        when(ausenciaService.atualizarAusencia(eq(1), any(AusenciaDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/ausencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(ausenciaService).atualizarAusencia(eq(1), any(AusenciaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void atualizarAusenciaParcial_DeveRetornar200_QuandoProfessor() throws Exception {
        when(ausenciaService.atualizarAusencia(eq(1), any(AusenciaDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/ausencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(ausenciaService).atualizarAusencia(eq(1), any(AusenciaDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void deletar_DeveRetornar200() throws Exception {
        doNothing().when(ausenciaService).deletarAusencia(1);

        mockMvc.perform(delete("/api/ausencias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Ausência apagada com sucesso."));

        verify(ausenciaService).deletarAusencia(1);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void deletar_DeveRetornar200_QuandoProfessor() throws Exception {
        doNothing().when(ausenciaService).deletarAusencia(1);

        mockMvc.perform(delete("/api/ausencias/1"))
                .andExpect(status().isOk());

        verify(ausenciaService).deletarAusencia(1);
    }
}

