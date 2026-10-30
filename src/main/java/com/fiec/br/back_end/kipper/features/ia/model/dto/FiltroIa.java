package com.fiec.br.back_end.kipper.features.ia.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FiltroIa(
        Boolean foraDoEscopo,
        String resumoDoPedido,
        String termo,
        String status,
        String prioridade,
        Boolean urgente,
        String tipoNome,
        String localNome,
        String setorNome,
        String solicitanteNome,
        String tecnicoNome,
        String numeroPatrimonio,
        String aberturaDe,
        String aberturaAte,
        String finalizacaoDe,
        String finalizacaoAte,
        String ordem
) {}