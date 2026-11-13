package com.fiec.br.back_end.kipper.features.solicitacao.model.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record LocalDTO(
        @NotBlank(message = "O nome do local é obrigatório")
        String nome,

        UUID setorId
) {}
