package com.onePilates.agendamento.auth.service;

import com.onePilates.agendamento.administrador.repository.AdministradorRepository;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.auth.dto.LoginDTO;
import com.onePilates.agendamento.auth.dto.LoginResponseDTO;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.secretaria.repository.SecretariaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private SecretariaRepository secretariaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RabbitMQProducer rabbitMQ;

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_DeveEmitirJwtComRoleAluno_QuandoEmailForDeAluno() {
        Aluno aluno = new Aluno();
        aluno.setId(5L);
        aluno.setNome("Ana");
        aluno.setEmail("aluno@teste.com");
        aluno.setSenha("$2a$10$hash");
        aluno.setStatus(true);

        when(administradorRepository.findByEmail("aluno@teste.com")).thenReturn(Optional.empty());
        when(professorRepository.findByEmail("aluno@teste.com")).thenReturn(Optional.empty());
        when(secretariaRepository.findByEmail("aluno@teste.com")).thenReturn(Optional.empty());
        when(alunoRepository.findByEmail("aluno@teste.com")).thenReturn(Optional.of(aluno));
        when(passwordEncoder.matches("senha123", "$2a$10$hash")).thenReturn(true);
        when(jwtUtil.generateToken("aluno@teste.com", Role.ALUNO)).thenReturn("token-aluno");

        LoginDTO request = new LoginDTO();
        request.setEmail("aluno@teste.com");
        request.setSenha("senha123");

        LoginResponseDTO response = authService.authenticate(request);

        assertEquals("ALUNO", response.getRole());
        assertEquals("token-aluno", response.getToken());
        assertEquals(Role.ALUNO, response.getFuncionario().getRole());
        verify(funcionarioRepository, never()).findByEmail("aluno@teste.com");
    }
}
