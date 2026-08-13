package com.fiec.br.back_end.kipper.features.solicitacao.controller;

import com.fiec.br.back_end.kipper.features.solicitacao.model.dto.SolicitacaoResponseDTO;
import com.fiec.br.back_end.kipper.features.solicitacao.model.dto.SolicitacaoSearchFilterDTO;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.PrioridadeSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.StatusSolicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.service.SolicitacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    @GetMapping("/search")
    public ResponseEntity<Page<SolicitacaoResponseDTO>> search(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) StatusSolicitacao status,
            @RequestParam(required = false) PrioridadeSolicitacao prioridade,
            @RequestParam(required = false) UUID usuarioSolicitanteId,
            @RequestParam(required = false) UUID tecnicoResponsavelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        SolicitacaoSearchFilterDTO filtro = new SolicitacaoSearchFilterDTO(
                termo, status, prioridade, usuarioSolicitanteId, tecnicoResponsavelId, dataInicio, dataFim
        );
        return ResponseEntity.ok(solicitacaoService.search(filtro, pageable));
    }
}
