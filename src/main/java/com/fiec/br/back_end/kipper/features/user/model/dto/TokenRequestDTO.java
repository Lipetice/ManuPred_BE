package com.fiec.br.back_end.kipper.features.user.model.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequestDTO(
        @NotBlank(message = "O token do Firebase é obrigatório")
        String token
) {}