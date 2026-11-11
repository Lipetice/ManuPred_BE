package com.fiec.br.back_end.kipper.features.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequestDTO(
        @NotBlank(message = "O fcmToken é obrigatório")
        @Schema(description = "Token FCM do dispositivo móvel. Para testes, pode digitar qualquer texto.", example = "token-de-teste-do-celular")
        String fcmToken
) {}