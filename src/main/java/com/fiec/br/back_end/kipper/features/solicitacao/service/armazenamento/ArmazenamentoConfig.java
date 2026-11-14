package com.fiec.br.back_end.kipper.features.solicitacao.service.armazenamento;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Escolhe onde os anexos ficam guardados (app.storage.tipo / variável STORAGE_TIPO):
 *  - "auto" (padrão): usa o S3 se as credenciais da AWS estiverem configuradas; senão, pasta local;
 *  - "s3": sempre o S3 (bucket fiec-tcc-kipper, ou o de S3_BUCKET);
 *  - "local": sempre a pasta local (app.upload-dir).
 *
 * As credenciais e a região vêm do bloco cloud.aws do application.yaml, que lê as variáveis
 * AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY e AWS_REGION.
 */
@Slf4j
@Configuration
public class ArmazenamentoConfig {

    private static final String BUCKET_PADRAO = "fiec-tcc-kipper";
    private static final String REGIAO_PADRAO = "us-east-1";

    @Bean
    public ArmazenamentoArquivos armazenamentoArquivos(
            @Value("${app.storage.tipo:auto}") String tipo,
            @Value("${app.upload-dir:uploads}") String uploadDir,
            @Value("${app.storage.s3.bucket:" + BUCKET_PADRAO + "}") String bucket,
            @Value("${app.storage.s3.prefixo:anexos/}") String prefixo,
            @Value("${cloud.aws.region.static:" + REGIAO_PADRAO + "}") String regiao,
            @Value("${cloud.aws.credentials.access-key:}") String accessKey,
            @Value("${cloud.aws.credentials.secret-key:}") String secretKey) {

        String modo = tipo == null ? "" : tipo.trim().toLowerCase();
        boolean temCredenciais = naoVazio(accessKey) && naoVazio(secretKey);

        boolean usarS3 = switch (modo) {
            case "s3" -> true;
            case "local" -> false;
            // "auto", vazio ou qualquer outro valor: decide pelas credenciais
            default -> temCredenciais;
        };

        ArmazenamentoArquivos armazenamento;
        if (usarS3) {
            // Variáveis de ambiente vazias (secret não cadastrado) caem nos valores padrão
            armazenamento = new ArmazenamentoS3(
                    naoVazio(bucket) ? bucket.trim() : BUCKET_PADRAO,
                    naoVazio(regiao) ? regiao.trim() : REGIAO_PADRAO,
                    prefixo,
                    accessKey,
                    secretKey);
        } else {
            armazenamento = new ArmazenamentoLocal(naoVazio(uploadDir) ? uploadDir : "uploads");
        }

        log.info("Anexos serão guardados em: {}", armazenamento.descricao());
        return armazenamento;
    }

    private static boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }
}
