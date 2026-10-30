package com.fiec.br.back_end.kipper.features.ia.model.dto;

import com.fiec.br.back_end.kipper.features.solicitacao.model.dto.SolicitacaoResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record IaBuscaResponseDTO(
        String entendimento,
        Map<String, Object> filtrosAplicados,
        List<String> avisos,
        Page<SolicitacaoResponseDTO> resultados
) {}