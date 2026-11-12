package com.fiec.br.back_end.kipper.features.solicitacao.repositories;

import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Solicitacao;
import com.fiec.br.back_end.kipper.features.solicitacao.model.enums.StatusSolicitacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SolicitacaoRepository extends MongoRepository<Solicitacao, UUID> {
    long countByStatus(StatusSolicitacao status);

    // Sintaxe de Query do MongoDB (JSON)
    @Query("{ 'status': { $in: ?0 }, 'urgente': false }")
    List<Solicitacao> findAtivasParaSla(List<StatusSolicitacao> statuses);

    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    List<Solicitacao> findByPeriodo(LocalDateTime inicio, LocalDateTime fim);

    // Usados para impedir a exclusão de tipo/local que ainda tem chamados apontando para ele
    @Query(value = "{ 'tipo.$id': ?0 }", exists = true)
    boolean existsByTipoId(UUID tipoId);

    @Query(value = "{ 'local.$id': ?0 }", exists = true)
    boolean existsByLocalId(UUID localId);
}
