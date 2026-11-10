package com.fiec.br.back_end.kipper.features.solicitacao.model.entities;

import com.fiec.br.back_end.kipper.model.entities.Auditoria;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "tb_tipos_chamado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class TipoChamado extends Auditoria {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String nome;
    private String icone;
    private Integer slaHoras;
    private String acaoAoEstourar;
}