package com.fiec.br.back_end.kipper.features.solicitacao.model.dto;

import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.PrioridadeSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.StatusSolicitacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitacaoSearchFilterDTO(
        String termo,
        StatusSolicitacao status,
        PrioridadeSolicitacao prioridade,
        UUID usuarioSolicitanteId,
        UUID tecnicoResponsavelId,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {}
