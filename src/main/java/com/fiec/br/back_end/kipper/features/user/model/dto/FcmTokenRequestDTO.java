package com.fiec.br.back_end.kipper.features.user.model.dto;

import jakarta.validation.constraints.NotBlank;

/** Token do Firebase Cloud Messaging do aparelho (PATCH /api/v1/users/me/fcm-token). */
public record FcmTokenRequestDTO(
        @NotBlank(message = "O token FCM é obrigatório")
        String fcmToken
) {}
