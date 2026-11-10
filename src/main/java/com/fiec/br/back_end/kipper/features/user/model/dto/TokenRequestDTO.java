package com.fiec.br.back_end.kipper.features.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TokenRequestDTO(
        @NotBlank(message = "O token do Firebase é obrigatório")
        @Schema(description = "Token gerado pelo SDK do Firebase no celular. NÃO use 'string'.", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
        String token
) {}