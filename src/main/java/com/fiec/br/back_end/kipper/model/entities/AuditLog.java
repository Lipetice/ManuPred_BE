package com.fiec.br.back_end.kipper.model.entities;

import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "tb_audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id", callSuper = false)
public class AuditLog {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String acao;
    private String detalhe;
    @DBRef
    private Users autor;
    private LocalDateTime criadoEm;
}