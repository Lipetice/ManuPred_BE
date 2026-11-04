package com.fiec.br.back_end.kipper.features.auth.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record GoogleTokenRequestDTO(
        @NotBlank(message = "O idToken do Google é obrigatório")
        @Schema(description = "Token gerado pelo Google no app mobile ou web. NÃO use 'string'.", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
        String idToken
) {}