package com.onePilates.agendamento.auth.service;

import com.onePilates.agendamento.administrador.repository.AdministradorRepository;
import com.onePilates.agendamento.auth.dto.LoginDTO;
import com.onePilates.agendamento.auth.dto.LoginResponseDTO;
import com.onePilates.agendamento.auth.dto.NovaSenhaResponseDTO;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.email.dto.CodigoAcessoEmailDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.especialidade.dto.EspecialidadeResponseDTO;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.exception.CodigoExpiradoException;
import com.onePilates.agendamento.exception.CodigoInvalidoException;
import com.onePilates.agendamento.exception.CredenciaisInvalidasException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.exception.PerfilInativoException;
import com.onePilates.agendamento.funcionario.dto.FuncionarioLoginDTO;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Funcionario;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.model.enums.TipoEmail;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.secretaria.repository.SecretariaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AdministradorRepository administradorRepository;
    private final ProfessorRepository professorRepository;
    private final SecretariaRepository secretariaRepository;
    private final PasswordEncoder passwordEncoder;
    private final FuncionarioRepository funcionarioRepository;
    private final JwtUtil jwtUtil;
    private final RabbitMQProducer rabbitMQ;

    public AuthService(AdministradorRepository administradorRepository, ProfessorRepository professorRepository, SecretariaRepository secretariaRepository, PasswordEncoder passwordEncoder, FuncionarioRepository funcionarioRepository, JwtUtil jwtUtil, RabbitMQProducer rabbitMQ) {
        this.administradorRepository = administradorRepository;
        this.professorRepository = professorRepository;
        this.secretariaRepository = secretariaRepository;
        this.passwordEncoder = passwordEncoder;
        this.funcionarioRepository = funcionarioRepository;
        this.jwtUtil = jwtUtil;
        this.rabbitMQ = rabbitMQ;
    }

    public LoginResponseDTO authenticate(LoginDTO request) {
        logger.info("Tentativa de autenticação para email: {}", request.getEmail());
        try {
            Funcionario funcionario = buscarFuncionarioPorEmail(request.getEmail());

            if (funcionario == null) {
                throw new EntidadeNaoEncontradaException("Funcionário não encontrado");
            }

            if (!passwordEncoder.matches(request.getSenha(), funcionario.getSenha())) {
                throw new CredenciaisInvalidasException("Credenciais inválidas");
            }
            if (funcionario.getStatus() == null || funcionario.getStatus() == false) {
                throw new PerfilInativoException("Perfil inativo, contate o administrador do sistema para reativação");
            }

        String token = jwtUtil.generateToken(funcionario);

        FuncionarioLoginDTO funcionarioDTO;

        if (funcionario.getRole() == Role.PROFESSOR && funcionario instanceof Professor professor) {

            List<EspecialidadeResponseDTO> especialidades = professor.getEspecialidades()
                    .stream()
                    .map(e -> new EspecialidadeResponseDTO(e.getId(), e.getNome()))
                    .collect(Collectors.toList());

            funcionarioDTO = new FuncionarioLoginDTO(
                    funcionario.getId(),
                    funcionario.getNome(),
                    funcionario.getEmail(),
                    funcionario.getRole(),
                    funcionario.getCpf(),
                    funcionario.getDataNascimento(),
                    funcionario.getStatus(),
                    funcionario.getFoto(),
                    funcionario.getObservacoes(),
                    funcionario.getNotificacaoAtiva(),
                    funcionario.getCargo(),
                    funcionario.getEndereco(),
                    funcionario.getTelefone(),
                    especialidades,
                    funcionario.getPrimeiroAcesso()
            );


        } else {

            funcionarioDTO = new FuncionarioLoginDTO(
                    funcionario.getId(),
                    funcionario.getNome(),
                    funcionario.getEmail(),
                    funcionario.getRole(),
                    funcionario.getCpf(),
                    funcionario.getDataNascimento(),
                    funcionario.getStatus(),
                    funcionario.getFoto(),
                    funcionario.getObservacoes(),
                    funcionario.getNotificacaoAtiva(),
                    funcionario.getCargo(),
                    funcionario.getEndereco(),
                    funcionario.getTelefone(),
                    funcionario.getPrimeiroAcesso()
            );
            }

            LoginResponseDTO response = new LoginResponseDTO(token, funcionario.getRole().name(), funcionarioDTO);
            logger.info("Autenticação bem-sucedida para email: {}", request.getEmail());
            return response;
        } catch (BusinessException e) {
            logger.warn("Falha na autenticação para email {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado na autenticação para email: {}", request.getEmail(), e);
            throw e;
        }
    }



    private Funcionario buscarFuncionarioPorEmail(String email) {
        return administradorRepository.findByEmail(email)
                .map(f -> (Funcionario) f)
                .or(() -> professorRepository.findByEmail(email).map(f -> (Funcionario) f))
                .or(() -> secretariaRepository.findByEmail(email).map(f -> (Funcionario) f))
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado"));
    }

    @Transactional
    public String criarCodigoVerificacao(String email) {
        logger.info("Tentativa de criar código de verificação para email: {}", email);
        try {
            Funcionario funcionario = buscarFuncionarioPorEmail(email);

            String codigoVerificacao = String.format("%05d", new Random().nextInt(100000));
            funcionario.setCodigoVerificacao(codigoVerificacao);
            funcionario.setDataUltimaCriacaoCodigo(LocalDateTime.now());

            funcionarioRepository.save(funcionario);

            CodigoAcessoEmailDTO codigoAcessoEmailDTO = new CodigoAcessoEmailDTO();
            codigoAcessoEmailDTO.setCodigo(codigoVerificacao);
            codigoAcessoEmailDTO.setNomeFuncionario(funcionario.getNome());

            EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
            emailRequestDTO.setPayload(codigoAcessoEmailDTO);
            emailRequestDTO.setTypeEmail(TipoEmail.CODIGO_ACESSO);
            emailRequestDTO.setDestinatario(funcionario.getEmail());


            try {
                rabbitMQ.enviarPraFilaDeEmails(emailRequestDTO);
                logger.info("Código de verificação criado e enviado para a fila de emails");
            } catch (Exception e) {
                throw new OperacaoInvalidaException("Erro ao enviar o e-mail de verificação para a fila de emails.");
            }

            return "Código enviado para o e-mail do funcionário.";
        } catch (BusinessException e) {
            logger.warn("Falha ao criar código de verificação para email {}: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar código de verificação para email: {}", email, e);
            throw e;
        }
    }


    public Boolean validarCodigoVerificacao(String email, String codigoVerificacao) {
        logger.info("Tentativa de validar código de verificação para email: {}", email);
        try {
            Funcionario funcionario = buscarFuncionarioPorEmail(email);

            if (!funcionario.getCodigoVerificacao().equals(codigoVerificacao)) {
                throw new CodigoInvalidoException("Código inválido");
            }

            LocalDateTime agora = LocalDateTime.now();
            Duration diferenca = Duration.between(funcionario.getDataUltimaCriacaoCodigo(), agora);

            if (diferenca.toMinutes() > 5) {
                throw new CodigoExpiradoException("Código expirado");
            }

            logger.info("Código de verificação validado com sucesso para email: {}", email);
            return true;
        } catch (BusinessException e) {
            logger.warn("Falha ao validar código de verificação para email {}: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao validar código de verificação para email: {}", email, e);
            throw e;
        }
    }

    @Transactional
    public NovaSenhaResponseDTO novaSenha(String senha, String email) {
        logger.info("Tentativa de alterar senha para email: {}", email);
        try {
            Funcionario funcionario = buscarFuncionarioPorEmail(email);
            if (funcionario == null) {
                throw new EntidadeNaoEncontradaException("Funcionário não encontrado");
            }
            if(funcionario.getPrimeiroAcesso().equals(true)){
                funcionario.setPrimeiroAcesso(false);
            }
            funcionario.setSenha(passwordEncoder.encode(senha));
            funcionarioRepository.save(funcionario);
            NovaSenhaResponseDTO dtoResponse = new NovaSenhaResponseDTO();
            dtoResponse.setMensagem("Senha alterada com sucesso");
            logger.info("Senha alterada com sucesso para email: {}", email);
            return dtoResponse;
        } catch (BusinessException e) {
            logger.warn("Falha ao alterar senha para email {}: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao alterar senha para email: {}", email, e);
            throw e;
        }
    }
}
