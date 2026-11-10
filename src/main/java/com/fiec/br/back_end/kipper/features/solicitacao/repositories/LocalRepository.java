package com.fiec.br.back_end.kipper.features.solicitacao.repositories;

import com.fiec.br.back_end.kipper.features.solicitacao.model.entities.Local;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.UUID;

public interface LocalRepository extends MongoRepository<Local, UUID> {
    List<Local> findBySetorId(UUID setorId);
}