package com.onePilates.agendamento.especialidade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.especialidade.dto.EspecialidadeDTO;
import com.onePilates.agendamento.especialidade.dto.EspecialidadeResponseDTO;
import com.onePilates.agendamento.especialidade.service.EspecialidadeService;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.professor.dto.ProfessorPorEspecialidadeResponseDTO;
import com.onePilates.agendamento.sala.dto.SalasPorEspecialidadeResponseDTO;
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

@WebMvcTest(EspecialidadeController.class)
@Import(TestSecurityConfig.class)
class EspecialidadeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EspecialidadeService especialidadeService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private EspecialidadeDTO dto;
    private EspecialidadeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        dto = new EspecialidadeDTO();
        dto.setNome("Pilates Clássico");

        responseDTO = new EspecialidadeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNome("Pilates Clássico");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void criarEspecialidade_DeveRetornar200() throws Exception {
        Especialidade especialidade = new Especialidade();
        especialidade.setId(1L);
        when(especialidadeService.criarEspecialidade(any(EspecialidadeDTO.class))).thenReturn(especialidade);
        when(especialidadeService.toResponseDTO(any(Especialidade.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(especialidadeService).criarEspecialidade(any(EspecialidadeDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void criarEspecialidade_DeveRetornar403_QuandoProfessor() throws Exception {
        mockMvc.perform(post("/api/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(especialidadeService, never()).criarEspecialidade(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void listarEspecialidades_DeveRetornar200() throws Exception {
        List<EspecialidadeResponseDTO> lista = Arrays.asList(responseDTO);
        when(especialidadeService.listarTodasDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/especialidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(especialidadeService).listarTodasDTO();
    }

    @Test
    @WithMockUser(authorities = {"PROFESSOR"})
    void listarEspecialidades_DeveRetornar200_QuandoProfessor() throws Exception {
        List<EspecialidadeResponseDTO> lista = Arrays.asList(responseDTO);
        when(especialidadeService.listarTodasDTO()).thenReturn(lista);

        mockMvc.perform(get("/api/especialidades"))
                .andExpect(status().isOk());

        verify(especialidadeService).listarTodasDTO();
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarPorId_DeveRetornar200() throws Exception {
        when(especialidadeService.buscarPorIdDTO(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/especialidades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(especialidadeService).buscarPorIdDTO(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void atualizarEspecialidadeParcial_DeveRetornar200() throws Exception {
        when(especialidadeService.atualizarEspecialidade(eq(1L), any(EspecialidadeDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(patch("/api/especialidades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(especialidadeService).atualizarEspecialidade(eq(1L), any(EspecialidadeDTO.class));
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void excluirEspecialidade_DeveRetornar204() throws Exception {
        doNothing().when(especialidadeService).excluirEspecialidade(1L);

        mockMvc.perform(delete("/api/especialidades/1"))
                .andExpect(status().isNoContent());

        verify(especialidadeService).excluirEspecialidade(1L);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void excluirEspecialidade_DeveRetornar403_QuandoSecretaria() throws Exception {
        mockMvc.perform(delete("/api/especialidades/1"))
                .andExpect(status().isForbidden());

        verify(especialidadeService, never()).excluirEspecialidade(any());
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void BuscarProfessorEspecialidade_DeveRetornar200() throws Exception {
        List<ProfessorPorEspecialidadeResponseDTO> lista = Arrays.asList(new ProfessorPorEspecialidadeResponseDTO());
        when(especialidadeService.BuscarProfessor(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/especialidades/professores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(especialidadeService).BuscarProfessor(1L);
    }

    @Test
    @WithMockUser(authorities = {"SECRETARIA"})
    void BuscarProfessorEspecialidade_DeveRetornar200_QuandoSecretaria() throws Exception {
        List<ProfessorPorEspecialidadeResponseDTO> lista = Arrays.asList(new ProfessorPorEspecialidadeResponseDTO());
        when(especialidadeService.BuscarProfessor(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/especialidades/professores/1"))
                .andExpect(status().isOk());

        verify(especialidadeService).BuscarProfessor(1L);
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void buscarSalasPorEspecialidade_DeveRetornar200() throws Exception {
        List<SalasPorEspecialidadeResponseDTO> lista = Arrays.asList(new SalasPorEspecialidadeResponseDTO());
        when(especialidadeService.buscarSalasPorEspecialidade(1L)).thenReturn(lista);

        mockMvc.perform(get("/api/especialidades/salas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(especialidadeService).buscarSalasPorEspecialidade(1L);
    }
}

