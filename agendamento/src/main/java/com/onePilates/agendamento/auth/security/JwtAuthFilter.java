package com.onePilates.agendamento.auth.security;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Funcionario;
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
import java.util.Optional;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final FuncionarioRepository funcionarioRepository;
    private final AlunoRepository alunoRepository;

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

            String principal = emailDeQuemExiste(email);
            if (principal == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String role = jwtUtil.extractRole(token);
            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException ex) {
            // token inválido -> deixa sem autenticação
        }

        filterChain.doFilter(request, response);
    }

    private String emailDeQuemExiste(String email) {
        Optional<Funcionario> funcionario = funcionarioRepository.findByEmail(email);
        if (funcionario != null && funcionario.isPresent()) {
            return funcionario.get().getEmail();
        }
        if (alunoRepository == null) {
            return null;
        }
        Optional<Aluno> aluno = alunoRepository.findByEmail(email);
        if (aluno != null && aluno.isPresent()) {
            return aluno.get().getEmail();
        }
        return null;
    }
}
