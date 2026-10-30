package com.fiec.br.back_end.kipper.features.ia.controller;

import com.fiec.br.back_end.kipper.features.ia.model.dto.*;
import com.fiec.br.back_end.kipper.features.ia.service.IaService;
import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ia")
@RequiredArgsConstructor
@Tag(name = "Inteligência Artificial", description = "Endpoints do assistente de IA")
public class IaController {

    private final IaService iaService;

    private Users getUsuarioLogado() {
        return (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Operation(summary = "Verificar status da IA")
    @GetMapping("/status")
    public ResponseEntity<IaStatusDTO> status() {
        return ResponseEntity.ok(iaService.verificarStatus());
    }

    @Operation(summary = "Busca em linguagem natural", description = "Apenas ADMIN e SUPERVISOR.")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @PostMapping("/busca")
    public ResponseEntity<IaBuscaResponseDTO> buscar(@RequestBody @Valid IaBuscaRequestDTO request) {
        Users u = getUsuarioLogado();
        return ResponseEntity.ok(iaService.buscarComLinguagemNatural(request, u.getEmail()));
    }

    @Operation(summary = "Organizar descrição", description = "Qualquer usuário logado.")
    @PostMapping("/organizar-descricao")
    public ResponseEntity<OrganizarDescricaoResponseDTO> organizar(@RequestBody @Valid OrganizarDescricaoRequestDTO request) {
        Users u = getUsuarioLogado();
        return ResponseEntity.ok(iaService.organizarDescricao(request, u.getEmail()));
    }

    @Operation(summary = "Gerar resumo do chamado", description = "Apenas ADMIN, SUPERVISOR e FUNCIONARIO.")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','FUNCIONARIO')")
    @GetMapping("/solicitacoes/{id}/resumo")
    public ResponseEntity<ResumoSolicitacaoResponseDTO> resumo(@PathVariable UUID id) {
        Users u = getUsuarioLogado();
        return ResponseEntity.ok(iaService.gerarResumoSolicitacao(id, u.getEmail()));
    }
}