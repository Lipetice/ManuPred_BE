package com.fiec.br.back_end.kipper.features.solicitacao.repositories;

import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Movimentacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.UUID;

public interface MovimentacaoRepository extends MongoRepository<Movimentacao, UUID> {
    List<Movimentacao> findBySolicitacaoIdOrderByCreatedAtDesc(UUID solicitacaoId);
}