package com.fiec.br.back_end.kipper.model.service;

import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import com.fiec.br.back_end.kipper.model.entities.AuditLog;
import com.fiec.br.back_end.kipper.model.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void registrar(String acao, String detalhe, Users autor) {
        AuditLog log = AuditLog.builder()
                .acao(acao)
                .detalhe(detalhe)
                .autor(autor)
                .criadoEm(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> listar() {
        return auditLogRepository.findAllByOrderByCriadoEmDesc();
    }
}