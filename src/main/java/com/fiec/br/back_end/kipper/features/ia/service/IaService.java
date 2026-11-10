package com.fiec.br.back_end.kipper.features.ia.service;

import com.fiec.br.back_end.kipper.features.ia.model.dto.*;

import java.util.UUID;

public interface IaService {
    IaStatusDTO verificarStatus();
    IaBuscaResponseDTO buscarComLinguagemNatural(IaBuscaRequestDTO request, String emailUsuario);
    OrganizarDescricaoResponseDTO organizarDescricao(OrganizarDescricaoRequestDTO request, String emailUsuario);
    ResumoSolicitacaoResponseDTO gerarResumoSolicitacao(UUID solicitacaoId, String emailUsuario); // NOVO
}