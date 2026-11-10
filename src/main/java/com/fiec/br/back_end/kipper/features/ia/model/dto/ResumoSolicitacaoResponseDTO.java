package com.fiec.br.back_end.kipper.features.ia.model.dto;

import java.time.LocalDateTime;

public record ResumoSolicitacaoResponseDTO(
        String resumo,
        LocalDateTime geradoEm
) {}