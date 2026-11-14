package com.fiec.br.back_end.kipper.features.solicitacao.service.armazenamento;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Onde os anexos dos chamados ficam guardados. Existem duas implementações:
 *  - ArmazenamentoLocal: pasta no disco do servidor (padrão, bom para desenvolvimento);
 *  - ArmazenamentoS3: bucket do Amazon S3 (produção).
 * A escolha é feita pela propriedade app.storage.tipo (variável STORAGE_TIPO).
 *
 * O "caminho" é sempre relativo, no formato {idDoChamado}/{nomeDoArquivo}.
 */
public interface ArmazenamentoArquivos {

    void gravar(String caminho, InputStream conteudo, long tamanho, String contentType) throws IOException;

    /** Devolve o arquivo, ou vazio se ele não existir. */
    Optional<ArquivoArmazenado> abrir(String caminho) throws IOException;

    /** Apaga todos os arquivos de uma pasta (os anexos de um chamado). */
    void apagarPasta(String pasta);

    /** Texto curto para o log de inicialização, ex.: "S3 (bucket meu-bucket)". */
    String descricao();
}
