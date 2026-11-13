package com.fiec.br.back_end.kipper.features.solicitacao.model.entities;

import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.PrioridadeSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.StatusSolicitacao;
import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import com.fiec.br.back_end.kipper.model.entities.Auditoria;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.*;

@Document(collection = "tb_solicitacoes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class Solicitacao extends Auditoria {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String titulo;
    private String descricao;

    // Índices: os filtros mais usados na busca e no SLA
    @Indexed
    private StatusSolicitacao status;

    private PrioridadeSolicitacao prioridade;

    @DBRef
    private TipoChamado tipo;

    @DBRef
    private Local local;

    private String localizacaoProblema;
    private String numeroPatrimonio;

    @Indexed
    private Boolean urgente;

    /**
     * Fica true quando o SLA do chamado estoura. Serve para o scheduler tratar cada chamado
     * uma única vez (não aparece na resposta da API).
     */
    private Boolean slaEstourado;

    @DBRef
    private Users usuarioSolicitante;

    @DBRef
    private Users tecnicoResponsavel;

    @DBRef
    @Builder.Default
    private List<Users> ajudantes = new ArrayList<>();

    private LocalDateTime dataFinalizacao;
    private LocalDateTime dataConclusao;
    private String motivoRejeicao;
}
