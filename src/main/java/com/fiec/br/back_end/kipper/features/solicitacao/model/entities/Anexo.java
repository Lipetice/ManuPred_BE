package com.fiec.br.back_end.kipper.features.solicitacao.model.entities;

import com.fiec.br.back_end.kipper.model.entities.Auditoria;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "tb_anexos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class Anexo extends Auditoria {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String nomeArquivo;
    private String caminhoArmazenado;
    private String tipoConteudo;
    @DBRef
    private Solicitacao solicitacao;
}