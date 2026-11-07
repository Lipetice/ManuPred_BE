package com.fiec.br.back_end.kipper.features.solicitacao.model.entities;

import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import com.fiec.br.back_end.kipper.model.entities.Auditoria;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "tb_movimentacoes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class Movimentacao extends Auditoria {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String titulo;
    private String descricao;
    @DBRef
    private Users autor;
    @DBRef
    private Solicitacao solicitacao;
}