package com.onePilates.agendamento.agendamento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.dto.AgendamentoResponseDTO;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.enums.StatusPresenca;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgendamentoController.class)
@Import(TestSecurityConfig.class)
@SuppressWarnings("unchecked")
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AgendamentoService agendamentoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private AgendamentoDTO dto;
    private AgendamentoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new AgendamentoDTO();
        dto.setDataHora(LocalDateTime.now().plusDays(1));
        dto.setSalaId(1L);
        dto.setProfessorId(1L);
        dto.setEspecialidadeId(1L);
        dto.setAlunoIds(Set.of(1L, 2L));

        responseDTO = new AgendamentoResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setDataHora(dto.getDataHora());
        responseDTO.setProfessor("Professor Teste");
        responseDTO.setSala("Sala 1");
        responseDTO.setEspecialidade("Pilates Clássico");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void criarAgendamento_DeveRetornar200_QuandoDadosValidos() throws Exception {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);

        when(agendamentoService.criarAgendamento(any(AgendamentoDTO.class))).thenReturn(agendamento);
        when(agendamentoService.toResponseDTO(any(Agendamento.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(agendamentoService).criarAgendamento(any(AgendamentoDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void criarAgendamento_DeveRetornar200_QuandoSecretaria() throws Exception {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);

        when(agendamentoService.criarAgendamento(any(AgendamentoDTO.class))).thenReturn(agendamento);
        when(agendamentoService.toResponseDTO(any(Agendamento.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(agendamentoService).criarAgendamento(any(AgendamentoDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void criarAgendamento_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(agendamentoService, never()).criarAgendamento(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarAgendamentos_DeveRetornar200() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(agendamentoService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void listarAgendamentos_DeveRetornar200_QuandoSecretaria() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isOk());

        verify(agendamentoService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void listarAgendamentos_DeveRetornar200_QuandoProfessor() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.listarTodosDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isOk());

        verify(agendamentoService).listarTodosDTO();
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorId_DeveRetornar200_QuandoEncontrado() throws Exception {
        when(agendamentoService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/agendamentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(agendamentoService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void buscarPorId_DeveRetornar200_QuandoProfessor() throws Exception {
        when(agendamentoService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/agendamentos/1"))
                .andExpect(status().isOk());

        verify(agendamentoService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarAgendamentoPorSalaEProfessor_DeveRetornar200() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.buscarAgendamentosPorIdsDeSalaEProfessor(1L, 2L)).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(agendamentoService).buscarAgendamentosPorIdsDeSalaEProfessor(1L, 2L);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void buscarAgendamentoPorSalaEProfessor_DeveRetornar200_QuandoSecretaria() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.buscarAgendamentosPorIdsDeSalaEProfessor(1L, 2L)).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos/1/2"))
                .andExpect(status().isOk());

        verify(agendamentoService).buscarAgendamentosPorIdsDeSalaEProfessor(1L, 2L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void buscarAgendamentoPorSalaEProfessor_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(get("/api/agendamentos/1/2"))
                .andExpect(status().isForbidden());

        verify(agendamentoService, never()).buscarAgendamentosPorIdsDeSalaEProfessor(any(), any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void atualizarAgendamentoParcial_DeveRetornar200() throws Exception {
        when(agendamentoService.atualizarAgendamento(eq(1L), any(AgendamentoDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/agendamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(agendamentoService).atualizarAgendamento(eq(1L), any(AgendamentoDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void atualizarAgendamentoParcial_DeveRetornar200_QuandoSecretaria() throws Exception {
        when(agendamentoService.atualizarAgendamento(eq(1L), any(AgendamentoDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/agendamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(agendamentoService).atualizarAgendamento(eq(1L), any(AgendamentoDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void atualizarAgendamentoParcial_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(patch("/api/agendamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(agendamentoService, never()).atualizarAgendamento(any(), any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void excluirAgendamento_DeveRetornar204() throws Exception {
        doNothing().when(agendamentoService).excluirAgendamento(1L);

        mockMvc.perform(delete("/api/agendamentos/1"))
                .andExpect(status().isNoContent());

        verify(agendamentoService).excluirAgendamento(1L);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void excluirAgendamento_DeveRetornar204_QuandoSecretaria() throws Exception {
        doNothing().when(agendamentoService).excluirAgendamento(1L);

        mockMvc.perform(delete("/api/agendamentos/1"))
                .andExpect(status().isNoContent());

        verify(agendamentoService).excluirAgendamento(1L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void excluirAgendamento_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(delete("/api/agendamentos/1"))
                .andExpect(status().isForbidden());

        verify(agendamentoService, never()).excluirAgendamento(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarPorProfessorId_DeveRetornar200() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.buscarAgendamentosPorIdProfessor(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos/professorId/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(agendamentoService).buscarAgendamentosPorIdProfessor(1L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void listarPorProfessorId_DeveRetornar200_QuandoProfessor() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.buscarAgendamentosPorIdProfessor(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos/professorId/1"))
                .andExpect(status().isOk());

        verify(agendamentoService).buscarAgendamentosPorIdProfessor(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void agendamentosPorSala_DeveRetornar200() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.buscarAgendamentosPorIdSala(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos/sala/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(agendamentoService).buscarAgendamentosPorIdSala(1L);
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void agendamentosPorSala_DeveRetornar200_QuandoProfessor() throws Exception {
        List<AgendamentoResponseDTO> lista = Arrays.asList(responseDTO);
        when(agendamentoService.buscarAgendamentosPorIdSala(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/agendamentos/sala/1"))
                .andExpect(status().isOk());

        verify(agendamentoService).buscarAgendamentosPorIdSala(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void registrarPresenca_DeveRetornar200() throws Exception {
        Map<Long, StatusPresenca> presencas = new HashMap<>();
        presencas.put(1L, StatusPresenca.PRESENTE);
        presencas.put(2L, StatusPresenca.FALTA);

        doNothing().when(agendamentoService).registrarPresencas(eq(1L), any(Map.class));

        mockMvc.perform(patch("/api/agendamentos/1/presenca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presencas)))
                .andExpect(status().isOk());

        verify(agendamentoService).registrarPresencas(eq(1L), any(Map.class));
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void registrarPresenca_DeveRetornar200_QuandoSecretaria() throws Exception {
        Map<Long, StatusPresenca> presencas = new HashMap<>();
        presencas.put(1L, StatusPresenca.PRESENTE);

        doNothing().when(agendamentoService).registrarPresencas(eq(1L), any(Map.class));

        mockMvc.perform(patch("/api/agendamentos/1/presenca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presencas)))
                .andExpect(status().isOk());

        verify(agendamentoService).registrarPresencas(eq(1L), any(Map.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void registrarPresenca_DeveRetornar200_QuandoProfessor() throws Exception {
        Map<Long, StatusPresenca> presencas = new HashMap<>();
        presencas.put(1L, StatusPresenca.PRESENTE);

        doNothing().when(agendamentoService).registrarPresencas(eq(1L), any(Map.class));

        mockMvc.perform(patch("/api/agendamentos/1/presenca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(presencas)))
                .andExpect(status().isOk());

        verify(agendamentoService).registrarPresencas(eq(1L), any(Map.class));
    }
}

