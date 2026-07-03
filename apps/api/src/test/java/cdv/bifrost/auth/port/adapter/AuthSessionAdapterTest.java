package cdv.bifrost.auth.port.adapter;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSessionAdapterTest extends IntegrationTestBase {

    @Test
    void save_persistsAndReturnsMappedSession() {
        User user = userSavePort.save(User.builder()
                .email("session-adapter@bifrost.local")
                .name("n")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        AuthSession session = AuthSession.builder()
                .userId(user.getId())
                .sessionHash("adapter-hash-1")
                .ipAddress("127.0.0.1")
                .userAgent("agent")
                .lastSeenAt(now)
                .expiresAt(now.plusHours(1))
                .build();

        AuthSession saved = sessionSavePort.save(session);

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getSessionHash()).isEqualTo("adapter-hash-1");
        assertThat(authSessionRepository.findBySessionHash("adapter-hash-1")).isPresent();
    }

    @Test
    void findByHash_returnsMappedDomainOrEmpty() {
        User user = userSavePort.save(User.builder()
                .email("session-find@bifrost.local")
                .name("n")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());

        sessionSavePort.save(AuthSession.builder()
                .userId(user.getId())
                .sessionHash("find-hash")
                .lastSeenAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(1))
                .build());

        assertThat(sessionReadPort.findByHash("find-hash")).isPresent();
        assertThat(sessionReadPort.findByHash("missing")).isEmpty();
    }

    @Test
    void touch_updatesLastSeenAt() throws InterruptedException {
        User user = userSavePort.save(User.builder()
                .email("session-touch@bifrost.local")
                .name("n")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());

        sessionSavePort.save(AuthSession.builder()
                .userId(user.getId())
                .sessionHash("touch-hash")
                .lastSeenAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(1))
                .build());

        var before = authSessionRepository.findBySessionHash("touch-hash").orElseThrow();
        var initialLastSeen = before.getLastSeenAt();
        Thread.sleep(20);

        sessionSavePort.touch("touch-hash");

        var after = authSessionRepository.findBySessionHash("touch-hash").orElseThrow();
        assertThat(after.getLastSeenAt()).isAfter(initialLastSeen);
    }

    @Test
    void touch_isNoOp_whenHashMissing() {
        sessionSavePort.touch("never-existed");
    }

    @Test
    void revokeByHash_marksRevokedAt() {
        User user = userSavePort.save(User.builder()
                .email("session-revoke@bifrost.local")
                .name("n")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());

        sessionSavePort.save(AuthSession.builder()
                .userId(user.getId())
                .sessionHash("revoke-hash")
                .lastSeenAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(1))
                .build());

        sessionRevokePort.revokeByHash("revoke-hash");

        var after = authSessionRepository.findBySessionHash("revoke-hash").orElseThrow();
        assertThat(after.getRevokedAt()).isNotNull();
    }

    @Test
    void revokeByHash_isIdempotent_doesNotChangeRevokedAtOnSecondCall() {
        User user = userSavePort.save(User.builder()
                .email("session-revoke-idem@bifrost.local")
                .name("n")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());

        sessionSavePort.save(AuthSession.builder()
                .userId(user.getId())
                .sessionHash("revoke-idem")
                .lastSeenAt(LocalDateTime.now(ZoneOffset.UTC))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(1))
                .build());

        sessionRevokePort.revokeByHash("revoke-idem");
        var firstRevokedAt = authSessionRepository.findBySessionHash("revoke-idem").orElseThrow()
                .getRevokedAt();
        assertThat(firstRevokedAt).isNotNull();

        sessionRevokePort.revokeByHash("revoke-idem");
        var secondRevokedAt = authSessionRepository.findBySessionHash("revoke-idem").orElseThrow()
                .getRevokedAt();
        assertThat(secondRevokedAt).isEqualTo(firstRevokedAt);
    }

    @Test
    void revokeByHash_isNoOp_whenHashMissing() {
        sessionRevokePort.revokeByHash("never-existed");
    }
}
