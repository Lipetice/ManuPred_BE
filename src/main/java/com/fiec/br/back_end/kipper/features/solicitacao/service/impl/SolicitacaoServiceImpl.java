package com.fiec.br.back_end.kipper.features.solicitacao.service.impl;

import com.fiec.br.back_end.kipper.features.solicitacao.model.dto.SolicitacaoResponseDTO;
import com.fiec.br.back_end.kipper.features.solicitacao.model.dto.SolicitacaoSearchFilterDTO;
import com.fiec.br.back_end.kipper.features.solicitacao.repositories.SolicitacaoRepository;
import com.fiec.br.back_end.kipper.features.solicitacao.repositories.specification.SolicitacaoSpecification;
import com.fiec.br.back_end.kipper.features.solicitacao.service.SolicitacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolicitacaoServiceImpl implements SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitacaoResponseDTO> search(SolicitacaoSearchFilterDTO filtro, Pageable pageable) {
        return solicitacaoRepository
                .findAll(SolicitacaoSpecification.comFiltros(filtro), pageable)
                .map(SolicitacaoResponseDTO::fromEntity);
    }
}
