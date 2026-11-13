package com.fiec.br.back_end.kipper.features.solicitacao.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TipoChamadoDTO(
        @NotBlank(message = "O nome do tipo é obrigatório")
        String nome,

        String icone,

        @Min(value = 1, message = "O SLA deve ser de pelo menos 1 hora")
        Integer slaHoras,

        String acaoAoEstourar
) {}
