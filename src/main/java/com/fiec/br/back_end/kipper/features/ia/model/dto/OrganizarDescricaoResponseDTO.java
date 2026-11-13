package com.fiec.br.back_end.kipper.features.ia.model.dto;

public record OrganizarDescricaoResponseDTO(
        String descricao,
        String tipoSugeridoId,
        String tipoSugeridoNome,
        Boolean pareceUrgente,
        String motivoUrgencia
) {}