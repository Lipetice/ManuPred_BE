package com.fiec.br.back_end.kipper.features.solicitacao.service;

import com.fiec.br.back_end.kipper.features.solicitacao.service.armazenamento.ArmazenamentoArquivos;
import com.fiec.br.back_end.kipper.features.solicitacao.service.armazenamento.ArquivoArmazenado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Valida e guarda os anexos dos chamados. Onde eles ficam (pasta local ou Amazon S3) é decidido
 * pelo ArmazenamentoArquivos configurado (ver ArmazenamentoConfig).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileStorageService {

    private final ArmazenamentoArquivos armazenamento;

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

        // A extensão é OBRIGATÓRIA: é ela que define como o arquivo é entregue em /uploads.
        boolean extensaoValida = EXTENSOES_PERMITIDAS.stream().anyMatch(nomeOriginal::endsWith);
        boolean contentTypeValido = contentType.isEmpty() || CONTENT_TYPES_PERMITIDOS.contains(contentType);

        if (!extensaoValida || !contentTypeValido) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não permitido: " + arquivo.getOriginalFilename() +
                            ". Só são aceitos imagens (jpg, jpeg, png, gif, webp, bmp) ou arquivos CSV."
            );
        }
    }

    /**
     * Guarda o anexo e devolve o caminho relativo ({idDoChamado}/{nome}), que é gravado em
     * Anexo.caminhoArmazenado. O formato é o mesmo de antes, então os anexos antigos continuam válidos.
     */
    public String salvar(UUID solicitacaoId, MultipartFile arquivo) {
        validar(arquivo);

        String nomeArmazenado = UUID.randomUUID() + "_" + sanitizarNome(arquivo.getOriginalFilename());
        String caminho = solicitacaoId + "/" + nomeArmazenado;

        try (InputStream in = arquivo.getInputStream()) {
            armazenamento.gravar(caminho, in, arquivo.getSize(), tipoDoArquivo(nomeArmazenado).toString());
        } catch (IOException | RuntimeException e) {
            log.error("Falha ao salvar o anexo {}: {}", caminho, e.getMessage());
            throw new RuntimeException("Falha ao salvar anexo: " + e.getMessage(), e);
        }
        return caminho;
    }

    /** Abre um anexo pelo caminho relativo. Vazio se não existir. */
    public Optional<ArquivoArmazenado> abrir(String caminho) {
        try {
            return armazenamento.abrir(caminho);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler anexo: " + e.getMessage(), e);
        }
    }

    /**
     * Apaga os anexos de um chamado excluído. Se algum arquivo não puder ser apagado,
     * a exclusão do chamado segue normalmente (o arquivo só fica sobrando no armazenamento).
     */
    public void apagarArquivos(UUID solicitacaoId) {
        try {
            armazenamento.apagarPasta(solicitacaoId.toString());
        } catch (RuntimeException e) {
            log.warn("Não foi possível apagar os anexos do chamado {}: {}", solicitacaoId, e.getMessage());
        }
    }

    /** Tipo do arquivo pela extensão (não confia no que o cliente mandou). */
    public static MediaType tipoDoArquivo(String nome) {
        if (nome != null && nome.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            return MediaType.parseMediaType("text/csv");
        }
        return MediaTypeFactory.getMediaType(nome).orElse(MediaType.APPLICATION_OCTET_STREAM);
    }

    /**
     * Mantém só o nome do arquivo (sem pastas) e troca espaços, acentos e símbolos por "_".
     * O nome original continua guardado em Anexo.nomeArquivo para exibição.
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
