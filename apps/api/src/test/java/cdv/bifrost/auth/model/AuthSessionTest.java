package cdv.bifrost.auth.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSessionTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 15, 12, 0, 0)
            .atZone(ZoneOffset.UTC).toLocalDateTime();

    @Test
    void isActive_returnsTrue_whenNotRevokedAndNotExpired() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW.plusHours(1))
                .build();

        assertThat(session.isActive(NOW)).isTrue();
        assertThat(session.isRevoked()).isFalse();
        assertThat(session.isExpired(NOW)).isFalse();
    }

    @Test
    void isRevoked_returnsTrue_whenRevokedAtIsNotNull() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW.plusHours(1))
                .revokedAt(NOW.minusMinutes(1))
                .build();

        assertThat(session.isRevoked()).isTrue();
        assertThat(session.isActive(NOW)).isFalse();
    }

    @Test
    void isExpired_returnsTrue_whenExpiresAtEqualsNow() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW)
                .build();

        assertThat(session.isExpired(NOW)).isTrue();
        assertThat(session.isActive(NOW)).isFalse();
    }

    @Test
    void isExpired_returnsTrue_whenExpiresAtIsBeforeNow() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW.minusHours(1))
                .build();

        assertThat(session.isExpired(NOW)).isTrue();
    }

    @Test
    void isExpired_returnsFalse_whenExpiresAtIsAfterNow() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW.plusSeconds(1))
                .build();

        assertThat(session.isExpired(NOW)).isFalse();
    }

    @Test
    void isExpired_returnsFalse_whenExpiresAtIsNull() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .build();

        assertThat(session.isExpired(NOW)).isFalse();
    }

    @Test
    void isActive_returnsFalse_whenRevokedAndExpired() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW.minusHours(1))
                .revokedAt(NOW.minusHours(2))
                .build();

        assertThat(session.isActive(NOW)).isFalse();
    }

    @Test
    void isActive_returnsFalse_whenRevokedButNotExpired() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW.plusHours(1))
                .revokedAt(NOW.minusMinutes(1))
                .build();

        assertThat(session.isActive(NOW)).isFalse();
    }

    @Test
    void isActive_returnsFalse_whenExpiredButNotRevoked() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW.minusSeconds(1))
                .build();

        assertThat(session.isActive(NOW)).isFalse();
    }

    @Test
    void builder_supportsInheritedBaseDomainModelFields() {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("u")
                .sessionHash("h")
                .expiresAt(NOW.plusHours(1))
                .build();

        assertThat(session.getId()).isEqualTo("s");
        assertThat(session.getUserId()).isEqualTo("u");
        assertThat(session.getSessionHash()).isEqualTo("h");
    }
}
