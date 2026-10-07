package com.fiec.br.back_end.kipper.features.solicitacao.model.dto;

import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Solicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.PrioridadeSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.StatusSolicitacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SolicitacaoResponseDTO(
        UUID id,
        String titulo,
        String descricao,
        StatusSolicitacao status,
        PrioridadeSolicitacao prioridade,
        UUID tipoId,
        String tipoNome,
        String tipoIcone,
        UUID localId,
        String localNome,
        UUID setorId,
        String setorNome,
        String numeroPatrimonio,
        String localizacaoProblema,
        Boolean urgente,
        UUID usuarioSolicitanteId,
        String usuarioSolicitanteNome,
        UUID tecnicoResponsavelId,
        String tecnicoResponsavelNome,
        List<AjudanteDTO> ajudantes,
        LocalDateTime dataAbertura,
        LocalDateTime dataAtualizacao,
        LocalDateTime dataConclusao,
        LocalDateTime dataFinalizacao,
        String motivoRejeicao,
        List<AnexoResponseDTO> anexos
) {
    public static SolicitacaoResponseDTO fromEntity(Solicitacao s) {
        return fromEntity(s, List.of());
    }

    public static SolicitacaoResponseDTO fromEntity(Solicitacao s, List<AnexoResponseDTO> anexos) {
        return new SolicitacaoResponseDTO(
                s.getId(),
                s.getTitulo(),
                s.getDescricao(),
                s.getStatus(),
                s.getPrioridade(),
                s.getTipo() != null ? s.getTipo().getId() : null,
                s.getTipo() != null ? s.getTipo().getNome() : null,
                s.getTipo() != null ? s.getTipo().getIcone() : null,
                s.getLocal() != null ? s.getLocal().getId() : null,
                s.getLocal() != null ? s.getLocal().getNome() : null,
                s.getLocal() != null && s.getLocal().getSetor() != null ? s.getLocal().getSetor().getId() : null,
                s.getLocal() != null && s.getLocal().getSetor() != null ? s.getLocal().getSetor().getNome() : null,
                s.getNumeroPatrimonio(),
                s.getLocalizacaoProblema(),
                s.getUrgente(),
                s.getUsuarioSolicitante() != null ? s.getUsuarioSolicitante().getId() : null,
                s.getUsuarioSolicitante() != null ? s.getUsuarioSolicitante().getName() : null,
                s.getTecnicoResponsavel() != null ? s.getTecnicoResponsavel().getId() : null,
                s.getTecnicoResponsavel() != null ? s.getTecnicoResponsavel().getName() : null,
                s.getAjudantes() != null ? s.getAjudantes().stream().map(AjudanteDTO::fromEntity).toList() : List.of(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                s.getDataConclusao(),
                s.getDataFinalizacao(),
                s.getMotivoRejeicao(),
                anexos
        );
    }
}