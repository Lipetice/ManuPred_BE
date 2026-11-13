package com.fiec.br.back_end.kipper.features.ia.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SugestaoDescricaoIa(
        String descricao,
        String tipoNome,
        Boolean pareceUrgente,
        String motivoUrgencia
) {}