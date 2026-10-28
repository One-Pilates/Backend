package com.onePilates.agendamento.imagem.controller;

import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.imagem.service.ImagemService;
import com.onePilates.agendamento.model.Funcionario;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/imagens")
@CrossOrigin(origins = "*")
public class ImagemController {

    private static final Path DIRETORIO_IMAGENS = Paths.get("imagens").toAbsolutePath().normalize();
    private final FuncionarioRepository funcionarioRepository;
    private final ImagemService imagemService;

    public ImagemController(FuncionarioRepository funcionarioRepository, ImagemService imagemService) {
        this.funcionarioRepository = funcionarioRepository;
        this.imagemService = imagemService;
    }

    @GetMapping("/funcionarios/{id}")
    public ResponseEntity<Resource> servirImagemFuncionario(@PathVariable Long id) {
        Funcionario funcionario = funcionarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Funcionário não encontrado"));

        if (funcionario.getFoto() == null || funcionario.getFoto().isBlank()) {
            return ResponseEntity.notFound().build();
        }

        ImagemService.DownloadImagem download = imagemService.baixarObjeto(funcionario.getFoto());
        MediaType mediaType = download.contentType() != null && !download.contentType().isBlank()
                ? MediaType.parseMediaType(download.contentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new org.springframework.core.io.ByteArrayResource(download.conteudo()));
    }

    @GetMapping({"", "/{*caminho}"})
    public ResponseEntity<Resource> servirImagem(@PathVariable(required = false) String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        String caminhoRelativo = caminho.replace('\\', '/');
        if (caminhoRelativo.startsWith("imagens/")) {
            caminhoRelativo = caminhoRelativo.substring("imagens/".length());
        }
        Path arquivo = DIRETORIO_IMAGENS.resolve(caminhoRelativo).normalize();
        if (!arquivo.startsWith(DIRETORIO_IMAGENS) || !Files.isRegularFile(arquivo)) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaTypeFactory.getMediaType(arquivo.getFileName().toString())
            .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header("Content-Disposition", "inline; filename=\"" + arquivo.getFileName() + "\"")
                .body(new FileSystemResource(arquivo));
    }
}
