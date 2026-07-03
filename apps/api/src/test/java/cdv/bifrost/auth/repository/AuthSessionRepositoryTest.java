package cdv.bifrost.auth.repository;

import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthSessionRepositoryTest extends IntegrationTestBase {

    @Autowired
    private AuthSessionRepository authSessionRepository;

    private UserEntity seedUser() {
        return userRepository.save(UserEntity.builder()
                .email("session-repo@bifrost.local")
                .displayName("Session Repo")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());
    }

    @Test
    void findBySessionHash_returnsHitAndMiss() {
        UserEntity user = seedUser();
        AuthSessionEntity entity = AuthSessionEntity.builder()
                .userId(user.getId())
                .sessionHash("hash-1")
                .ipAddress("127.0.0.1")
                .userAgent("agent")
                .lastSeenAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .build();
        authSessionRepository.save(entity);

        Optional<AuthSessionEntity> hit = authSessionRepository.findBySessionHash("hash-1");
        assertThat(hit).isPresent();
        assertThat(hit.get().getUserId()).isEqualTo(user.getId());

        assertThat(authSessionRepository.findBySessionHash("nonexistent")).isEmpty();
    }

    @Test
    void uniqueConstraintOnSessionHash_throwsOnDuplicate() {
        UserEntity user = seedUser();
        authSessionRepository.save(AuthSessionEntity.builder()
                .userId(user.getId())
                .sessionHash("dup-hash")
                .lastSeenAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .build());

        assertThatThrownBy(() -> authSessionRepository.saveAndFlush(AuthSessionEntity.builder()
                .userId(user.getId())
                .sessionHash("dup-hash")
                .lastSeenAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void cascadeDelete_removesSessionsWhenUserDeleted() {
        UserEntity user = seedUser();
        authSessionRepository.save(AuthSessionEntity.builder()
                .userId(user.getId())
                .sessionHash("cascade-hash")
                .lastSeenAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusHours(1))
                .build());

        assertThat(authSessionRepository.findBySessionHash("cascade-hash")).isPresent();

        userRepository.deleteById(user.getId());
        userRepository.flush();

        assertThat(authSessionRepository.findBySessionHash("cascade-hash")).isEmpty();
    }

    @Test
    void canStoreAndRetrieveRevokedAndExpiredSessions() {
        UserEntity user = seedUser();
        OffsetDateTime now = OffsetDateTime.now();

        AuthSessionEntity revoked = AuthSessionEntity.builder()
                .userId(user.getId())
                .sessionHash("revoked-hash")
                .lastSeenAt(now)
                .expiresAt(now.plusHours(1))
                .revokedAt(now)
                .build();
        AuthSessionEntity expired = AuthSessionEntity.builder()
                .userId(user.getId())
                .sessionHash("expired-hash")
                .lastSeenAt(now.minusHours(2))
                .expiresAt(now.minusHours(1))
                .build();

        authSessionRepository.save(revoked);
        authSessionRepository.save(expired);

        AuthSessionEntity foundRevoked = authSessionRepository.findBySessionHash("revoked-hash").orElseThrow();
        assertThat(foundRevoked.getRevokedAt()).isNotNull();

        AuthSessionEntity foundExpired = authSessionRepository.findBySessionHash("expired-hash").orElseThrow();
        assertThat(foundExpired.getExpiresAt()).isBefore(now);
    }
}
