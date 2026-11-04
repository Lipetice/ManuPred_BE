package com.fiec.br.back_end.kipper.features.auth.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        @Schema(description = "Nome completo do usuário", example = "João Silva")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Schema(description = "E-mail do usuário", example = "joao@teste.com")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        @Schema(description = "Senha (mínimo 6 caracteres)", example = "senha123")
        String password,

        // O @Schema(hidden = true) faz o Swagger ignorar esse campo na tela
        @Schema(hidden = true)
        String fcmToken
) {}