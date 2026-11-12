package com.fiec.br.back_end.kipper.features.user.repositories;

import com.fiec.br.back_end.kipper.features.user.model.entities.UserRole;
import com.fiec.br.back_end.kipper.features.user.model.entities.Users;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends MongoRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByFirebaseUid(String firebaseUid);
    boolean existsByEmail(String email);
    List<Users> findAllByFcmToken(String fcmToken);
    List<Users> findByNameContainingIgnoreCase(String nome);
    boolean existsByRole(UserRole role);
}
