package com.fiec.br.back_end.kipper.features.solicitacao.service.armazenamento;

import java.io.InputStream;

/**
 * Um arquivo lido do armazenamento.
 *
 * @param conteudo o conteúdo do arquivo (quem recebe é responsável por fechar)
 * @param tamanho  tamanho em bytes, ou -1 se não for conhecido
 */
public record ArquivoArmazenado(InputStream conteudo, long tamanho) {
}
