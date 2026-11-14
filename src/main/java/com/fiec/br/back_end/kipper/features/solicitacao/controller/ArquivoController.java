package com.fiec.br.back_end.kipper.features.solicitacao.controller;

import com.fiec.br.back_end.kipper.exception.RecursoNaoEncontradoException;
import com.fiec.br.back_end.kipper.features.solicitacao.service.FileStorageService;
import com.fiec.br.back_end.kipper.features.solicitacao.service.armazenamento.ArquivoArmazenado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Entrega os anexos em /uploads/{idDoChamado}/{arquivo}, venham eles da pasta local ou do S3.
 * O endereço é o mesmo de antes (campo "url" do AnexoResponseDTO), então o app e o site
 * não precisam mudar nada.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Anexos", description = "Download das imagens e arquivos anexados aos chamados")
public class ArquivoController {

    private static final Pattern NOME_SEGURO = Pattern.compile("[A-Za-z0-9._-]{1,200}");

    private final FileStorageService fileStorageService;

    @Operation(summary = "Baixar anexo",
            description = "Use o valor do campo 'url' que vem nos anexos do chamado. Não precisa de login.")
    @SecurityRequirements // sem cadeado no Swagger: a rota é pública
    @GetMapping("/uploads/{pasta}/{arquivo}")
    public ResponseEntity<InputStreamResource> baixar(@PathVariable String pasta, @PathVariable String arquivo) {
        // Só aceita {UUID}/{nome-simples}: impede caminhos como ../../ para fora da pasta
        if (!ehUuid(pasta) || !NOME_SEGURO.matcher(arquivo).matches() || arquivo.contains("..")) {
            throw new RecursoNaoEncontradoException("Arquivo não encontrado.");
        }

        ArquivoArmazenado anexo = fileStorageService.abrir(pasta + "/" + arquivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Arquivo não encontrado."));

        ResponseEntity.BodyBuilder resposta = ResponseEntity.ok()
                .contentType(FileStorageService.tipoDoArquivo(arquivo))
                // O nome do arquivo é único (tem um UUID), então o navegador pode guardar em cache
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(arquivo).build().toString());

        if (anexo.tamanho() >= 0) {
            resposta.contentLength(anexo.tamanho());
        }
        return resposta.body(new InputStreamResource(anexo.conteudo()));
    }

    private boolean ehUuid(String valor) {
        try {
            UUID.fromString(valor);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
