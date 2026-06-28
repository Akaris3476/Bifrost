package cdv.bifrost.auth.repository;

import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, UUID> {

    Optional<AuthSessionEntity> findBySessionHash(String sessionHash);
}
