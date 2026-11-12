package com.fiec.br.back_end.kipper.features.solicitacao.model.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record SetorDTO(
        @NotBlank(message = "O nome do setor é obrigatório")
        String nome,

        UUID responsavelId
) {}
