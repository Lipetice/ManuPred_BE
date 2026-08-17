package com.fiec.br.back_end.kipper.features.solicitacao.model.dto;

import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Solicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.PrioridadeSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.StatusSolicitacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitacaoResponseDTO(
        UUID id,
        String titulo,
        String descricao,
        StatusSolicitacao status,
        PrioridadeSolicitacao prioridade,
        UUID usuarioSolicitanteId,
        String usuarioSolicitanteNome,
        UUID tecnicoResponsavelId,
        String tecnicoResponsavelNome,
        LocalDateTime createdAt
) {
    public static SolicitacaoResponseDTO fromEntity(Solicitacao s) {
        return new SolicitacaoResponseDTO(
                s.getId(),
                s.getTitulo(),
                s.getDescricao(),
                s.getStatus(),
                s.getPrioridade(),
                s.getUsuarioSolicitante() != null ? s.getUsuarioSolicitante().getId() : null,
                s.getUsuarioSolicitante() != null ? s.getUsuarioSolicitante().getName() : null,
                s.getTecnicoResponsavel() != null ? s.getTecnicoResponsavel().getId() : null,
                s.getTecnicoResponsavel() != null ? s.getTecnicoResponsavel().getName() : null,
                s.getCreatedAt()
        );
    }
}
