package com.onePilates.agendamento.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class AlunoSwaggerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documento_DevePublicarRotasDeAluno() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();
        String json = resultado.getResponse().getContentAsString();

        assertTrue(json.contains("\"/api/alunos\""));
        assertTrue(json.contains("\"/api/alunos/{id}\""));
        assertTrue(json.contains("\"/api/alunos/{id}/pacotes\""));
        assertTrue(json.contains("\"/api/alunos/{alunoId}/pacotes/{especialidadeId}\""));
        assertTrue(json.contains("\"/api/aluno/agenda\""));
        assertTrue(json.contains("\"/api/aluno/agenda/{agendamentoId}/presenca\""));
        assertTrue(json.contains("\"/api/aluno/agenda/{agendamentoId}\""));
        assertTrue(json.contains("\"/api/aluno/agenda/{agendamentoId}/feedback\""));
        assertTrue(json.contains("\"/api/aluno/perfil\""));
        assertTrue(json.contains("\"/api/aluno/perfil/foto\""));
        assertTrue(json.contains("\"/api/aluno/dashboard/{qtdUltimosDias}\""));
        assertTrue(json.contains("\"/api/aluno/pacotes\""));
        assertTrue(json.contains("Aulas futuras"));
        assertTrue(json.contains("saldo absoluto"));
        assertTrue(json.contains("BearerAuth"));
    }

    @Test
    void rotasDeAluno_DeveRetornar401_SemToken() throws Exception {
        mockMvc.perform(get("/api/aluno/agenda")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/alunos")).andExpect(status().isUnauthorized());
    }

    @Test
    void professor_DeveReceber403_AoGravarPacote() throws Exception {
        String token = login("professor@teste.com");

        mockMvc.perform(put("/api/alunos/1/pacotes/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aulasRemanescentes\":10}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void aluno_DeveConsultarSoOProprioPacote() throws Exception {
        String token = login("aluno1@teste.com");

        mockMvc.perform(get("/api/alunos/1/pacotes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/alunos/2/pacotes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void secretaria_DeveReceber403_AoCancelarPeloPortal() throws Exception {
        String token = login("secretaria@teste.com");

        mockMvc.perform(delete("/api/aluno/agenda/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorEAluno_DevemExercitarAsOperacoesDoSwagger() throws Exception {
        String admin = login("admin@teste.com");

        mockMvc.perform(get("/api/alunos").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/alunos/1").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Aluno 1"));
        mockMvc.perform(patch("/api/alunos/1")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Aluno 1\"}"))
                .andExpect(status().isOk());

        MvcResult criado = mockMvc.perform(post("/api/alunos")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Aluno Novo","email":"novo@teste.com","cpf":"33333333333","status":true,
                                 "endereco":{"rua":"Rua A","numero":"1","bairro":"Centro","cidade":"Sao Paulo","estado":"SP","cep":"01001000","uf":"SP"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        String corpo = criado.getResponse().getContentAsString();
        String idNovo = corpo.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(delete("/api/alunos/" + idNovo).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/alunos/1/pacotes").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/alunos/1/pacotes/1")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aulasRemanescentes\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aulasRemanescentes").value(10));

        String aluno = login("aluno1@teste.com");
        mockMvc.perform(get("/api/aluno/agenda").header("Authorization", "Bearer " + aluno))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/aluno/pacotes").header("Authorization", "Bearer " + aluno))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/aluno/perfil").header("Authorization", "Bearer " + aluno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Aluno 1"));
        mockMvc.perform(patch("/api/aluno/perfil")
                        .header("Authorization", "Bearer " + aluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nao Muda\",\"notificacaoAtiva\":true,\"tipoContato\":\"email\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/aluno/perfil").header("Authorization", "Bearer " + aluno))
                .andExpect(jsonPath("$.nome").value("Aluno 1"))
                .andExpect(jsonPath("$.telefone").value("email"));

        mockMvc.perform(patch("/api/aluno/agenda/1/presenca").header("Authorization", "Bearer " + aluno))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/aluno/dashboard/30").header("Authorization", "Bearer " + aluno))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/aluno/agenda/2/feedback")
                        .header("Authorization", "Bearer " + aluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":5,\"comentario\":\"boa\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/aluno/agenda/2/feedback")
                        .header("Authorization", "Bearer " + aluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":4}"))
                .andExpect(status().isConflict());
        mockMvc.perform(patch("/api/aluno/agenda/2/feedback")
                        .header("Authorization", "Bearer " + aluno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":3}"))
                .andExpect(status().isOk());

        MockMultipartFile foto = new MockMultipartFile("foto", "a.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/api/aluno/perfil/foto").file(foto)
                        .header("Authorization", "Bearer " + aluno))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(containsString("alunos/1/")));

        mockMvc.perform(delete("/api/aluno/agenda/1").header("Authorization", "Bearer " + aluno))
                .andExpect(status().isNoContent());
    }

    private String login(String email) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        String corpo = resultado.getResponse().getContentAsString();
        return corpo.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
