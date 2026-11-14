package com.fiec.br.back_end.kipper.features.solicitacao.service.armazenamento;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/** Guarda os anexos numa pasta do disco (app.upload-dir). */
public class ArmazenamentoLocal implements ArmazenamentoArquivos {

    private final Path raiz;

    public ArmazenamentoLocal(String uploadDir) {
        this.raiz = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void gravar(String caminho, InputStream conteudo, long tamanho, String contentType) throws IOException {
        Path destino = resolver(caminho);
        Files.createDirectories(destino.getParent());
        Files.copy(conteudo, destino);
    }

    @Override
    public Optional<ArquivoArmazenado> abrir(String caminho) throws IOException {
        Path arquivo = resolver(caminho);
        if (!Files.isRegularFile(arquivo)) {
            return Optional.empty();
        }
        return Optional.of(new ArquivoArmazenado(Files.newInputStream(arquivo), Files.size(arquivo)));
    }

    @Override
    public void apagarPasta(String pasta) {
        Path alvo;
        try {
            alvo = resolver(pasta);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (!Files.isDirectory(alvo)) return;

        try (Stream<Path> arquivos = Files.walk(alvo)) {
            // Ordem inversa: primeiro os arquivos, por último a própria pasta
            arquivos.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // segue apagando o resto
                }
            });
        } catch (IOException ignored) {
            // pasta inacessível: não impede a exclusão do chamado
        }
    }

    @Override
    public String descricao() {
        return "pasta local (" + raiz + ")";
    }

    /** Monta o caminho absoluto e garante que ele não sai da pasta de uploads. */
    private Path resolver(String caminho) {
        Path destino = raiz.resolve(caminho).normalize();
        if (!destino.startsWith(raiz) || destino.equals(raiz)) {
            throw new IllegalArgumentException("Caminho de arquivo inválido: " + caminho);
        }
        return destino;
    }
}
