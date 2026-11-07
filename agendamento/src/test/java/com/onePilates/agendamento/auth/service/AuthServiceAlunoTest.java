package com.onePilates.agendamento.auth.service;

import com.onePilates.agendamento.administrador.repository.AdministradorRepository;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.auth.dto.LoginDTO;
import com.onePilates.agendamento.auth.dto.LoginResponseDTO;
import com.onePilates.agendamento.auth.dto.NovaSenhaResponseDTO;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.email.dto.CodigoAcessoEmailDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.exception.CodigoExpiradoException;
import com.onePilates.agendamento.exception.CodigoInvalidoException;
import com.onePilates.agendamento.exception.CredenciaisInvalidasException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.PerfilInativoException;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.model.enums.TipoEmail;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.secretaria.repository.SecretariaRepository;
import com.onePilates.agendamento.testsupport.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Login, código de verificação e nova senha do aluno (o fluxo de funcionários não muda). */
@ExtendWith(MockitoExtension.class)
class AuthServiceAlunoTest {

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

    private Aluno ana;

    @BeforeEach
    void setUp() {
        ana = Fixtures.aluno(1L, "Ana", "ana@teste.com");
        ana.setSenha("hash");
        ana.setPrimeiroAcesso(true);
        when(administradorRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(professorRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(secretariaRepository.findByEmail(any())).thenReturn(Optional.empty());
    }

    private LoginDTO login(String email, String senha) {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(email);
        dto.setSenha(senha);
        return dto;
    }

    // ------------------------------------------------------------------ login

    @Test
    void login_deAlunoComSenhaCertaGeraTokenComPerfilAluno() {
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);
        when(jwtUtil.generateToken("ana@teste.com", Role.ALUNO)).thenReturn("token-aluno");

        LoginResponseDTO resposta = authService.authenticate(login("ana@teste.com", "123456"));

        assertEquals("token-aluno", resposta.getToken());
        assertEquals("ALUNO", resposta.getRole());
        assertEquals("Ana", resposta.getFuncionario().getNome());
        assertEquals(Role.ALUNO, resposta.getFuncionario().getRole());
        assertEquals(Boolean.TRUE, resposta.getFuncionario().getPrimeiroAcesso());
    }

    @Test
    void login_deAlunoNaoDevolveAsObservacoesInternas() {
        ana.setObservacao("lesão no joelho (uso interno)");
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtil.generateToken(any(String.class), any(Role.class))).thenReturn("t");

        LoginResponseDTO resposta = authService.authenticate(login("ana@teste.com", "123456"));

        assertNull(resposta.getFuncionario().getObservacoes());
    }

    @Test
    void login_deAlunoComSenhaErradaEhRecusado() {
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class, () -> authService.authenticate(login("ana@teste.com", "errada")));

        verify(jwtUtil, never()).generateToken(any(String.class), any(Role.class));
    }

    @Test
    void login_deAlunoAntigoSemSenhaEhRecusadoSemQuebrar() {
        ana.setSenha(null);
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));

        assertThrows(CredenciaisInvalidasException.class, () -> authService.authenticate(login("ana@teste.com", "qualquer")));
    }

    @Test
    void login_deAlunoInativoEhRecusado() {
        ana.setStatus(false);
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThrows(PerfilInativoException.class, () -> authService.authenticate(login("ana@teste.com", "123456")));
    }

    @Test
    void login_deEmailQueNaoExisteEmLugarNenhum() {
        when(alunoRepository.findByEmail("x@teste.com")).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> authService.authenticate(login("x@teste.com", "1")));
    }

    // ------------------------------------------------- código de verificação

    @Test
    void criarCodigo_paraAlunoGravaOCodigoEEnfileiraOEmail() {
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));

        authService.criarCodigoVerificacao("ana@teste.com");

        assertEquals(5, ana.getCodigoVerificacao().length());
        assertTrue(ana.getDataUltimaCriacaoCodigo().isAfter(LocalDateTime.now().minusMinutes(1)));
        verify(alunoRepository).save(ana);
        ArgumentCaptor<EmailRequestDTO> captor = ArgumentCaptor.forClass(EmailRequestDTO.class);
        verify(rabbitMQ).enviarPraFilaDeEmails(captor.capture());
        assertEquals(TipoEmail.CODIGO_ACESSO, captor.getValue().getTypeEmail());
        assertEquals("ana@teste.com", captor.getValue().getDestinatario());
        assertEquals(ana.getCodigoVerificacao(), ((CodigoAcessoEmailDTO) captor.getValue().getPayload()).getCodigo());
    }

    @Test
    void validarCodigo_certoEDentroDosCincoMinutos() {
        ana.setCodigoVerificacao("12345");
        ana.setDataUltimaCriacaoCodigo(LocalDateTime.now().minusMinutes(2));
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));

        assertTrue(authService.validarCodigoVerificacao("ana@teste.com", "12345"));
    }

    @Test
    void validarCodigo_erradoEhRecusado() {
        ana.setCodigoVerificacao("12345");
        ana.setDataUltimaCriacaoCodigo(LocalDateTime.now());
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));

        assertThrows(CodigoInvalidoException.class, () -> authService.validarCodigoVerificacao("ana@teste.com", "00000"));
    }

    @Test
    void validarCodigo_semCodigoGeradoEhRecusadoSemQuebrar() {
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));

        assertThrows(CodigoInvalidoException.class, () -> authService.validarCodigoVerificacao("ana@teste.com", "12345"));
    }

    @Test
    void validarCodigo_expiradoEhRecusado() {
        ana.setCodigoVerificacao("12345");
        ana.setDataUltimaCriacaoCodigo(LocalDateTime.now().minusMinutes(6));
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));

        assertThrows(CodigoExpiradoException.class, () -> authService.validarCodigoVerificacao("ana@teste.com", "12345"));
    }

    // -------------------------------------------------------------- nova senha

    @Test
    void novaSenha_doAlunoGravaOHashETiraDoPrimeiroAcesso() {
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("hash-novo");

        NovaSenhaResponseDTO resposta = authService.novaSenha("NovaSenha1", "ana@teste.com");

        assertEquals("hash-novo", ana.getSenha());
        assertFalse(ana.getPrimeiroAcesso());
        assertEquals("Senha alterada com sucesso", resposta.getMensagem());
        verify(alunoRepository).save(ana);
    }

    @Test
    void novaSenha_deAlunoQueAindaNaoTinhaSenhaFunciona() {
        ana.setSenha(null);
        ana.setPrimeiroAcesso(null);
        when(alunoRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.encode("NovaSenha1")).thenReturn("hash-novo");

        authService.novaSenha("NovaSenha1", "ana@teste.com");

        assertEquals("hash-novo", ana.getSenha());
        assertFalse(ana.getPrimeiroAcesso());
    }
}
