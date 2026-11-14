package com.fiec.br.back_end.kipper.features.solicitacao.service.armazenamento;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.util.Optional;

/**
 * Guarda os anexos num bucket do Amazon S3.
 *
 * As credenciais NÃO ficam no código: vêm do bloco cloud.aws do application.yaml (variáveis
 * AWS_ACCESS_KEY_ID e AWS_SECRET_ACCESS_KEY). Se estiverem vazias, o SDK da AWS procura sozinho
 * em ~/.aws/credentials ou na role da máquina (EC2/ECS).
 *
 * O bucket pode continuar privado: os arquivos são entregues pelo próprio back em /uploads/...
 */
@Slf4j
public class ArmazenamentoS3 implements ArmazenamentoArquivos, AutoCloseable {

    private final S3Client s3;
    private final String bucket;
    private final String prefixo;

    public ArmazenamentoS3(String bucket, String regiao, String prefixo, String accessKey, String secretKey) {
        this.bucket = bucket;
        this.prefixo = normalizarPrefixo(prefixo);

        var builder = S3Client.builder().region(Region.of(regiao));
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())));
        }
        this.s3 = builder.build();
        verificarAcesso();
    }

    @Override
    public void gravar(String caminho, InputStream conteudo, long tamanho, String contentType) {
        PutObjectRequest requisicao = PutObjectRequest.builder()
                .bucket(bucket)
                .key(chave(caminho))
                .contentType(contentType)
                .build();
        s3.putObject(requisicao, RequestBody.fromInputStream(conteudo, tamanho));
    }

    @Override
    public Optional<ArquivoArmazenado> abrir(String caminho) {
        try {
            ResponseInputStream<GetObjectResponse> objeto = s3.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(chave(caminho))
                    .build());
            Long tamanho = objeto.response().contentLength();
            return Optional.of(new ArquivoArmazenado(objeto, tamanho != null ? tamanho : -1));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    public void apagarPasta(String pasta) {
        ListObjectsV2Request listagem = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(chave(pasta) + "/")
                .build();

        s3.listObjectsV2Paginator(listagem).contents().forEach(objeto ->
                s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objeto.key()).build()));
    }

    @Override
    public String descricao() {
        return "Amazon S3 (bucket " + bucket + ", pasta " + (prefixo.isEmpty() ? "/" : prefixo) + ")";
    }

    @Override
    public void close() {
        s3.close();
    }

    /**
     * Confere na inicialização se o bucket existe e se as credenciais funcionam. Se falhar, só
     * registra o erro (a aplicação sobe do mesmo jeito; apenas os anexos ficam indisponíveis).
     */
    private void verificarAcesso() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket S3 '{}' acessível.", bucket);
        } catch (S3Exception e) {
            log.error("Não foi possível acessar o bucket S3 '{}' (HTTP {}): confira o nome do bucket, a região "
                    + "e as permissões da credencial.", bucket, e.statusCode());
        } catch (RuntimeException e) {
            log.error("Não foi possível acessar o bucket S3 '{}': {}. Confira as credenciais da AWS "
                    + "(AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY).", bucket, e.getMessage());
        }
    }

    private String chave(String caminho) {
        return prefixo + caminho;
    }

    private static String normalizarPrefixo(String prefixo) {
        if (prefixo == null || prefixo.isBlank()) return "";
        String p = prefixo.trim().replaceAll("^/+", "");
        return p.endsWith("/") ? p : p + "/";
    }
}
