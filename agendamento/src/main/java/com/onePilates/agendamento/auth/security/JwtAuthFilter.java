package com.onePilates.agendamento.auth.security;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Funcionario;
import com.onePilates.agendamento.model.enums.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final FuncionarioRepository funcionarioRepository;
    private final AlunoRepository alunoRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, FuncionarioRepository funcionarioRepository) {
        this(jwtUtil, funcionarioRepository, null);
    }

    public JwtAuthFilter(JwtUtil jwtUtil, FuncionarioRepository funcionarioRepository, AlunoRepository alunoRepository) {
        this.jwtUtil = jwtUtil;
        this.funcionarioRepository = funcionarioRepository;
        this.alunoRepository = alunoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            if (jwtUtil.isTokenExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtUtil.extractEmail(token);
            if (email == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String role = jwtUtil.extractRole(token);

            // Token de aluno: o usuário vem da tabela de alunos, nunca de funcionários
            boolean tokenDeAluno = Role.ALUNO.name().equals(role);
            String emailAutenticado;
            if (tokenDeAluno) {
                Aluno aluno = alunoRepository == null ? null : alunoRepository.findByEmail(email).orElse(null);
                emailAutenticado = aluno == null ? null : aluno.getEmail();
            } else {
                Funcionario funcionario = funcionarioRepository.findByEmail(email).orElse(null);
                emailAutenticado = funcionario == null ? null : funcionario.getEmail();
            }
            if (emailAutenticado == null) {
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(emailAutenticado, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException ex) {
            // token inválido -> deixa sem autenticação
        }

        filterChain.doFilter(request, response);
    }
}
