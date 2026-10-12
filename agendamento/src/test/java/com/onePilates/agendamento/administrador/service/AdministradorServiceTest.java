package com.onePilates.agendamento.administrador.service;

import com.onePilates.agendamento.administrador.dto.AdministradorDTO;
import com.onePilates.agendamento.administrador.dto.AdministradorResponseDTO;
import com.onePilates.agendamento.administrador.repository.AdministradorRepository;
import com.onePilates.agendamento.endereco.dto.EnderecoDTO;
import com.onePilates.agendamento.endereco.repository.EnderecoRepository;
import com.onePilates.agendamento.exception.CpfJaCadastradoException;
import com.onePilates.agendamento.exception.EmailJaCadastradoException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.imagem.service.ImagemService;
import com.onePilates.agendamento.model.Administrador;
import com.onePilates.agendamento.model.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministradorServiceTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImagemService imagemService;

    @InjectMocks
    private AdministradorService administradorService;

    private AdministradorDTO dto;
    private Administrador administrador;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        dto = new AdministradorDTO();
        dto.setNome("Admin Teste");
        dto.setEmail("admin@teste.com");
        dto.setCpf("12345678900");
        dto.setIdade(LocalDate.of(1990, 1, 1));
        dto.setStatus(true);
        dto.setSenha("senha123");
        dto.setCargo("Gerente");
        dto.setNotificacaoAtiva(true);

        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setRua("Rua Teste");
        enderecoDTO.setNumero("123");
        enderecoDTO.setBairro("Bairro Teste");
        enderecoDTO.setCidade("Cidade Teste");
        enderecoDTO.setEstado("Estado Teste");
        enderecoDTO.setCep("12345678");
        enderecoDTO.setUf("SP");
        dto.setEndereco(enderecoDTO);

        administrador = new Administrador();
        administrador.setId(1L);
        administrador.setNome("Admin Teste");
        administrador.setEmail("admin@teste.com");
        administrador.setCpf("12345678900");
        administrador.setDataNascimento(LocalDate.of(1990, 1, 1));
        administrador.setStatus(true);
        administrador.setCargo("Gerente");

        endereco = new Endereco();
        endereco.setId(1L);
        endereco.setRua("Rua Teste");
        administrador.setEndereco(endereco);
    }

    @Test
    void criarAdministrador_DeveCriarComSucesso() {
        when(administradorRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(administradorRepository.existsByCpf(dto.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("encodedPassword");
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);

        Administrador result = administradorService.criarAdministrador(dto);

        assertNotNull(result);
        assertEquals("Admin Teste", result.getNome());
        // Quando não há imagem nem foto, save é chamado apenas 1 vez
        verify(administradorRepository, times(1)).save(any(Administrador.class));
        verify(enderecoRepository).save(any(Endereco.class));
        verify(passwordEncoder).encode(dto.getSenha());
    }

    @Test
    void criarAdministrador_DeveSalvarDuasVezes_QuandoTemImagem() {
        MultipartFile imagem = mock(MultipartFile.class);
        when(imagem.isEmpty()).thenReturn(false);
        dto.setImagem(imagem);

        when(administradorRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(administradorRepository.existsByCpf(dto.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("encodedPassword");
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);
        when(imagemService.uploadFotoAdministrador(any(MultipartFile.class), anyLong())).thenReturn("administradores/1/foto-perfil-123.jpg");

        Administrador result = administradorService.criarAdministrador(dto);

        assertNotNull(result);
        // Quando há imagem, save é chamado 2 vezes (uma para criar, outra para atualizar com foto)
        verify(administradorRepository, times(2)).save(any(Administrador.class));
        verify(imagemService).uploadFotoAdministrador(any(MultipartFile.class), anyLong());
    }

    @Test
    void criarAdministrador_DeveSalvarDuasVezes_QuandoTemFoto() {
        dto.setFoto("imagens/foto.jpg");

        when(administradorRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(administradorRepository.existsByCpf(dto.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("encodedPassword");
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);

        Administrador result = administradorService.criarAdministrador(dto);

        assertNotNull(result);
        // Quando há foto (String), save é chamado 2 vezes (uma para criar, outra para atualizar com foto)
        verify(administradorRepository, times(2)).save(any(Administrador.class));
    }

    @Test
    void criarAdministrador_DeveLancarExcecao_QuandoEmailJaExiste() {
        when(administradorRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(EmailJaCadastradoException.class, () -> {
            administradorService.criarAdministrador(dto);
        });

        verify(administradorRepository, never()).save(any());
    }

    @Test
    void criarAdministrador_DeveLancarExcecao_QuandoCpfJaExiste() {
        when(administradorRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(administradorRepository.existsByCpf(dto.getCpf())).thenReturn(true);

        assertThrows(CpfJaCadastradoException.class, () -> {
            administradorService.criarAdministrador(dto);
        });

        verify(administradorRepository, never()).save(any());
    }

    @Test
    void criarAdministrador_DeveCriarSemEndereco_QuandoEnderecoNulo() {
        dto.setEndereco(null);
        when(administradorRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(administradorRepository.existsByCpf(dto.getCpf())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("encodedPassword");
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);

        Administrador result = administradorService.criarAdministrador(dto);

        assertNotNull(result);
        verify(enderecoRepository, never()).save(any());
    }

    @Test
    void listarTodosDTO_DeveRetornarLista() {
        List<Administrador> administradores = Arrays.asList(administrador);
        when(administradorRepository.findAll()).thenReturn(administradores);

        List<AdministradorResponseDTO> result = administradorService.listarTodosDTO();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(administradorRepository).findAll();
    }

    @Test
    void buscarPorIdDTO_DeveRetornarAdministrador() {
        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));

        AdministradorResponseDTO result = administradorService.buscarPorIdDTO(1L);

        assertNotNull(result);
        assertEquals("Admin Teste", result.getNome());
        verify(administradorRepository).findById(1L);
    }

    @Test
    void buscarPorIdDTO_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(administradorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            administradorService.buscarPorIdDTO(1L);
        });
    }

    @Test
    void atualizarAdministrador_DeveAtualizarComSucesso() {
        AdministradorDTO dtoAtualizacao = new AdministradorDTO();
        dtoAtualizacao.setNome("Admin Atualizado");
        dtoAtualizacao.setStatus(false);

        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);

        AdministradorResponseDTO result = administradorService.atualizarAdministrador(1L, dtoAtualizacao);

        assertNotNull(result);
        verify(administradorRepository).findById(1L);
        verify(administradorRepository).save(any(Administrador.class));
    }

    @Test
    void atualizarAdministrador_DeveLancarExcecao_QuandoEmailJaExiste() {
        AdministradorDTO dtoAtualizacao = new AdministradorDTO();
        dtoAtualizacao.setEmail("novo@email.com");

        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(administradorRepository.existsByEmail("novo@email.com")).thenReturn(true);

        assertThrows(EmailJaCadastradoException.class, () -> {
            administradorService.atualizarAdministrador(1L, dtoAtualizacao);
        });
    }

    @Test
    void atualizarAdministrador_DeveLancarExcecao_QuandoCpfJaExiste() {
        AdministradorDTO dtoAtualizacao = new AdministradorDTO();
        dtoAtualizacao.setCpf("98765432100");

        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(administradorRepository.existsByCpf("98765432100")).thenReturn(true);

        assertThrows(CpfJaCadastradoException.class, () -> {
            administradorService.atualizarAdministrador(1L, dtoAtualizacao);
        });
    }

    @Test
    void atualizarAdministrador_DeveAtualizarSenha_QuandoSenhaFornecida() {
        AdministradorDTO dtoAtualizacao = new AdministradorDTO();
        dtoAtualizacao.setSenha("novaSenha");

        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(passwordEncoder.encode("novaSenha")).thenReturn("encodedNewPassword");
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);

        administradorService.atualizarAdministrador(1L, dtoAtualizacao);

        verify(passwordEncoder).encode("novaSenha");
    }

    @Test
    void salvarFoto_DeveSalvarComSucesso() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        administrador.setFoto("fotoAntiga.jpg");

        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(imagemService.uploadFotoAdministrador(eq(file), eq(1L)))
            .thenReturn("administradores/1/foto-perfil-123.jpg");
        when(administradorRepository.save(any(Administrador.class))).thenReturn(administrador);

        String result = administradorService.salvarFoto(1L, file);

        assertEquals("administradores/1/foto-perfil-123.jpg", result);
        verify(imagemService).uploadFotoAdministrador(file, 1L);
        verify(imagemService).removerObjeto("fotoAntiga.jpg");
        verify(administradorRepository).save(any(Administrador.class));
    }

    @Test
    void salvarFoto_DeveLancarExcecao_QuandoAdministradorNaoEncontrado() {
        MultipartFile file = mock(MultipartFile.class);

        when(administradorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            administradorService.salvarFoto(1L, file);
        });
    }

    @Test
    void excluirAdministrador_DeveExcluirComSucesso() {
        administrador.setFoto("foto.jpg");

        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        doNothing().when(imagemService).removerObjeto("foto.jpg");
        doNothing().when(administradorRepository).deleteById(1L);

        administradorService.excluirAdministrador(1L);

        verify(administradorRepository).findById(1L);
        verify(imagemService).removerObjeto("foto.jpg");
        verify(administradorRepository).deleteById(1L);
    }

    @Test
    void excluirAdministrador_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(administradorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            administradorService.excluirAdministrador(1L);
        });
    }

    @Test
    void excluirAdministrador_DeveExcluirSemFoto_QuandoFotoNula() {
        administrador.setFoto(null);

        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        doNothing().when(administradorRepository).deleteById(1L);

        administradorService.excluirAdministrador(1L);

        verify(imagemService, never()).removerObjeto(anyString());
        verify(administradorRepository).deleteById(1L);
    }

    @Test
    void toResponseDTO_DeveConverterCorretamente() {
        AdministradorResponseDTO result = administradorService.toResponseDTO(administrador);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Admin Teste", result.getNome());
        assertEquals("admin@teste.com", result.getEmail());
        assertEquals("12345678900", result.getCpf());
        assertNotNull(result.getEndereco());
    }
}

