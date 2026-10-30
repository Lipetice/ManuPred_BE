package com.fiec.br.back_end.kipper.features.solicitacao.model.dto;

import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.PrioridadeSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.StatusSolicitacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SolicitacaoSearchFilterDTO(
        UUID id,
        String termo,
        StatusSolicitacao status,
        PrioridadeSolicitacao prioridade,
        String numeroPatrimonio,
        String localizacaoProblema,
        List<UUID> usuarioSolicitanteIds, // Alterado para List
        List<UUID> tecnicoResponsavelIds, // Alterado para List
        UUID tipoId,                      // Novo
        UUID localId,                     // Novo
        UUID setorId,                     // Novo
        Boolean urgente,                  // Novo
        LocalDateTime dataAberturaInicio,
        LocalDateTime dataAberturaFim,
        LocalDateTime dataFinalizacaoInicio,
        LocalDateTime dataFinalizacaoFim
) {}