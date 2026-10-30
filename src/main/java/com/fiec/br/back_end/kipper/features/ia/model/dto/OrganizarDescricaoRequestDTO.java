package com.fiec.br.back_end.kipper.features.ia.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizarDescricaoRequestDTO(
        @NotBlank(message = "O texto é obrigatório")
        @Size(min = 5, max = 2000, message = "O texto deve ter entre 5 e 2000 caracteres")
        String texto,
        String localizacao
) {}