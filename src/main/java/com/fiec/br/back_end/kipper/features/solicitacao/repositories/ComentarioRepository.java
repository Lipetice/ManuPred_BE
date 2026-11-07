package com.fiec.br.back_end.kipper.features.solicitacao.repositories;

import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Comentario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.UUID;

public interface ComentarioRepository extends MongoRepository<Comentario, UUID> {
    List<Comentario> findBySolicitacaoIdOrderByCreatedAtAsc(UUID solicitacaoId);
}