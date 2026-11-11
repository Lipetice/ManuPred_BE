package com.fiec.br.back_end.kipper.model.repositories;

import com.fiec.br.back_end.kipper.model.entities.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends MongoRepository<AuditLog, UUID> {
    List<AuditLog> findAllByOrderByCriadoEmDesc();
}