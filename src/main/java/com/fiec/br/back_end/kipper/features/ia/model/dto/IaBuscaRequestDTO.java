package com.fiec.br.back_end.kipper.features.ia.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IaBuscaRequestDTO(
        @NotBlank(message = "A pergunta é obrigatória")
        @Size(min = 3, max = 500, message = "A pergunta deve ter entre 3 e 500 caracteres")
        String pergunta,
        @Min(0) Integer page,
        @Min(1) @Max(50) Integer size
) {}