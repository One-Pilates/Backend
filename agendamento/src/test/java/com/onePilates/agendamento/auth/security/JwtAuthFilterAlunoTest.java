package com.onePilates.agendamento.auth.security;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Administrador;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.testsupport.Fixtures;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterAlunoTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    private JwtAuthFilter filtro;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filtro = new JwtAuthFilter(jwtUtil, funcionarioRepository, alunoRepository);
        when(request.getHeader("Authorization")).thenReturn("Bearer tok");
        when(jwtUtil.isTokenExpired("tok")).thenReturn(false);
        when(jwtUtil.extractEmail("tok")).thenReturn("ana@teste.com");
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tokenDeAlunoAutenticaComAutoridadeALUNO() throws Exception {
        when(jwtUtil.extractRole("tok")).thenReturn("ALUNO");
        Aluno ana = Fixtures.aluno(1L, "Ana", "ana@teste.com");
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));

        filtro.doFilterInternal(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("ana@teste.com", auth.getName());
        assertEquals("ALUNO", auth.getAuthorities().iterator().next().getAuthority());
        verify(funcionarioRepository, never()).findByEmail("ana@teste.com");
        verify(chain).doFilter(request, response);
    }

    @Test
    void tokenDeAlunoDeAlunoQueFoiExcluidoNaoAutentica() throws Exception {
        when(jwtUtil.extractRole("tok")).thenReturn("ALUNO");
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.empty());

        filtro.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void tokenDeFuncionarioNuncaProcuraNaTabelaDeAlunos() throws Exception {
        when(jwtUtil.extractRole("tok")).thenReturn("PROFESSOR");
        Administrador funcionario = new Administrador();
        funcionario.setEmail("ana@teste.com");
        funcionario.setRole(Role.PROFESSOR);
        when(funcionarioRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(funcionario));

        filtro.doFilterInternal(request, response, chain);

        assertEquals("PROFESSOR", SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority());
        verify(alunoRepository, never()).findByEmail("ana@teste.com");
    }

    @Test
    void filtroSemRepositorioDeAlunosNaoAutenticaTokenDeAluno() throws Exception {
        JwtAuthFilter semAlunos = new JwtAuthFilter(jwtUtil, funcionarioRepository);
        when(jwtUtil.extractRole("tok")).thenReturn("ALUNO");

        semAlunos.doFilterInternal(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }
}
