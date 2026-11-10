package com.fiec.br.back_end.kipper.features.user.model.dto;

import com.fiec.br.back_end.kipper.features.user.model.entities.UserRole;

import java.util.UUID;

/**
 * Dados do usuário autenticado (GET /api/v1/users/me).
 * O app mobile usa o id e o role para decidir o que exibir
 * (ex: ações de técnico/supervisor, painel Kanban do funcionário).
 */
public record UserMeDTO(UUID id, String email, String nome, UserRole role) {}
