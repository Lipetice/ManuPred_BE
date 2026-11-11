package com.fiec.br.back_end.kipper.features.solicitacao.repositories;

import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.TipoChamado;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.UUID;

public interface TipoChamadoRepository extends MongoRepository<TipoChamado, UUID> {
    boolean existsByNome(String nome);
}