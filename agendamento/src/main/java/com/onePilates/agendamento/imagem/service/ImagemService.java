package com.onePilates.agendamento.imagem.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class ImagemService {

    private static final Path DIRETORIO_IMAGENS = Path.of("imagens").toAbsolutePath().normalize();

    public String uploadFotoPerfil(MultipartFile file, Long professorId) {
        return uploadFoto(file, professorId, "professores");
    }

    public String uploadFotoSecretaria(MultipartFile file, Long secretariaId) {
        return uploadFoto(file, secretariaId, "secretarias");
    }

    public String uploadFotoAdministrador(MultipartFile file, Long administradorId) {
        return uploadFoto(file, administradorId, "administradores");
    }

    private String uploadFoto(MultipartFile file, Long entidadeId, String pasta) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RuntimeException("Arquivo de imagem não pode ser vazio");
            }
            String extensao = obterExtensao(file.getOriginalFilename());
            String nomeArquivo = "foto-perfil-" + System.currentTimeMillis() + "." + extensao;
            Path diretorioDestino = DIRETORIO_IMAGENS.resolve(pasta).resolve(String.valueOf(entidadeId)).normalize();
            if (!diretorioDestino.startsWith(DIRETORIO_IMAGENS)) {
                throw new IOException("Caminho de imagem inválido");
            }

            Files.createDirectories(diretorioDestino);
            Path arquivoDestino = diretorioDestino.resolve(nomeArquivo);
            Files.copy(file.getInputStream(), arquivoDestino, StandardCopyOption.REPLACE_EXISTING);

            return DIRETORIO_IMAGENS.relativize(arquivoDestino).toString().replace('\\', '/');
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload da foto", e);
        }
    }

    private String obterExtensao(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "jpg";
        }

        int indicePonto = originalFilename.lastIndexOf('.');
        if (indicePonto < 0 || indicePonto == originalFilename.length() - 1) {
            return "jpg";
        }

        return originalFilename.substring(indicePonto + 1).toLowerCase();
    }

    public void removerObjeto(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolverCaminho(caminho, false));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover imagem", e);
        }
    }

    public DownloadImagem baixarObjeto(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            throw new RuntimeException("Caminho da imagem não pode ser vazio");
        }

        try {
            Path arquivo = resolverCaminho(caminho, true);
            return new DownloadImagem(Files.readAllBytes(arquivo), Files.probeContentType(arquivo));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler imagem", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar imagem", e);
        }
    }

    private Path resolverCaminho(String caminho, boolean exigirArquivo) {
        String caminhoRelativo = caminho.replace('\\', '/');
        if (caminhoRelativo.startsWith("imagens/")) {
            caminhoRelativo = caminhoRelativo.substring("imagens/".length());
        }

        Path arquivo = DIRETORIO_IMAGENS.resolve(caminhoRelativo).normalize();
        if (!arquivo.startsWith(DIRETORIO_IMAGENS)
                || (exigirArquivo && !Files.isRegularFile(arquivo))) {
            throw new RuntimeException("Imagem não encontrada");
        }
        return arquivo;
    }

    public record DownloadImagem(byte[] conteudo, String contentType) {
    }
}
