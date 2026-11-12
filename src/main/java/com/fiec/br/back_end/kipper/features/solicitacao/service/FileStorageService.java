package com.fiec.br.back_end.kipper.features.solicitacao.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private static final List<String> CONTENT_TYPES_PERMITIDOS = List.of(
            // imagens
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp",
            // csv
            "text/csv", "application/csv", "application/vnd.ms-excel", "text/plain",
            // alguns apps mobile mandam o arquivo sem tipo específico
            "application/octet-stream"
    );

    private static final List<String> EXTENSOES_PERMITIDAS = List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".csv"
    );

    /**
     * Valida o arquivo sem salvar. Chamado antes de criar o chamado, para não sobrar
     * um chamado gravado no banco quando o anexo é recusado.
     */
    public void validar(MultipartFile arquivo) {
        String contentType = arquivo.getContentType() != null
                ? arquivo.getContentType().toLowerCase(Locale.ROOT).split(";")[0].trim()
                : "";
        String nomeOriginal = arquivo.getOriginalFilename() != null
                ? arquivo.getOriginalFilename().toLowerCase(Locale.ROOT)
                : "";

        // A extensão é OBRIGATÓRIA: é ela que define como o arquivo é servido em /uploads.
        // Antes bastava o content-type bater (e "text/plain" estava na lista), então dava para
        // subir um .html e ele seria aberto como página no endereço da API.
        boolean extensaoValida = EXTENSOES_PERMITIDAS.stream().anyMatch(nomeOriginal::endsWith);
        boolean contentTypeValido = contentType.isEmpty() || CONTENT_TYPES_PERMITIDOS.contains(contentType);

        if (!extensaoValida || !contentTypeValido) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não permitido: " + arquivo.getOriginalFilename() +
                            ". Só são aceitos imagens (jpg, jpeg, png, gif, webp, bmp) ou arquivos CSV."
            );
        }
    }

    public String salvar(UUID solicitacaoId, MultipartFile arquivo) {
        validar(arquivo);

        try {
            Path pastaSolicitacao = Path.of(uploadDir, solicitacaoId.toString()).toAbsolutePath().normalize();
            Files.createDirectories(pastaSolicitacao);

            String nomeArmazenado = UUID.randomUUID() + "_" + sanitizarNome(arquivo.getOriginalFilename());
            Path destino = pastaSolicitacao.resolve(nomeArmazenado).normalize();

            if (!destino.startsWith(pastaSolicitacao)) {
                throw new IllegalArgumentException("Nome de arquivo inválido: " + arquivo.getOriginalFilename());
            }

            try (InputStream in = arquivo.getInputStream()) {
                Files.copy(in, destino);
            }

            return solicitacaoId + "/" + nomeArmazenado;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar anexo: " + e.getMessage(), e);
        }
    }

    /**
     * Mantém só o nome do arquivo (sem pastas) e troca espaços, acentos e símbolos por "_".
     * Assim o caminho nunca sai da pasta de uploads e a URL /uploads/... funciona sem precisar
     * de encoding no front. O nome original continua guardado em Anexo.nomeArquivo para exibição.
     */
    private String sanitizarNome(String nomeOriginal) {
        String nome = nomeOriginal != null ? nomeOriginal : "arquivo";
        int ultimaBarra = Math.max(nome.lastIndexOf('/'), nome.lastIndexOf('\\'));
        if (ultimaBarra >= 0) {
            nome = nome.substring(ultimaBarra + 1);
        }
        nome = nome.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (nome.length() > 100) {
            nome = nome.substring(nome.length() - 100); // preserva a extensão, que fica no fim
        }
        return nome.isBlank() ? "arquivo" : nome;
    }
}
