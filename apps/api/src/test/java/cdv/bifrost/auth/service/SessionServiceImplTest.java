package cdv.bifrost.auth.service;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.auth.port.SessionReadPort;
import cdv.bifrost.auth.port.SessionRevokePort;
import cdv.bifrost.auth.port.SessionSavePort;
import cdv.bifrost.auth.service.impl.SessionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    SessionReadPort sessionReadPort;

    @Mock
    SessionSavePort sessionSavePort;

    @Mock
    SessionRevokePort sessionRevokePort;

    @InjectMocks
    SessionServiceImpl sessionService;

    private static User activeUser() {
        return User.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .email("user@example.com")
                .name("Test")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void create_persistsHashedSession_andReturnsRawId() {
        when(sessionSavePort.save(any(AuthSession.class))).thenAnswer(inv -> inv.getArgument(0));

        String raw = sessionService.create(activeUser(), "127.0.0.1", "TestAgent");

        assertThat(raw).isNotBlank();
        assertThat(raw).doesNotContain("+", "/", "=");
        assertThat(sessionService.hash(raw)).hasSize(64);
    }

    @Test
    void findActiveByRawId_returnsNullWhenSessionMissing() {
        String raw = "abc";
        when(sessionReadPort.findByHash(sessionService.hash(raw))).thenReturn(Optional.empty());

        assertThat(sessionService.findActiveByRawId(raw)).isNull();
    }

    @Test
    void findActiveByRawId_returnsNullForExpiredSession() {
        String raw = "abc";
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        AuthSession expired = AuthSession.builder()
                .id("x")
                .userId("u")
                .sessionHash(sessionService.hash(raw))
                .expiresAt(now.minusHours(1))
                .build();
        when(sessionReadPort.findByHash(sessionService.hash(raw))).thenReturn(Optional.of(expired));

        assertThat(sessionService.findActiveByRawId(raw)).isNull();
    }

    @Test
    void findActiveByRawId_returnsNullForRevokedSession() {
        String raw = "abc";
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        AuthSession revoked = AuthSession.builder()
                .id("x")
                .userId("u")
                .sessionHash(sessionService.hash(raw))
                .expiresAt(now.plusMinutes(10))
                .revokedAt(now.minusHours(1))
                .build();
        when(sessionReadPort.findByHash(sessionService.hash(raw))).thenReturn(Optional.of(revoked));

        assertThat(sessionService.findActiveByRawId(raw)).isNull();
    }

    @Test
    void findActiveByRawId_returnsSessionWhenActive() {
        String raw = "abc";
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        AuthSession active = AuthSession.builder()
                .id("x")
                .userId("u")
                .sessionHash(sessionService.hash(raw))
                .expiresAt(now.plusMinutes(10))
                .build();
        when(sessionReadPort.findByHash(sessionService.hash(raw))).thenReturn(Optional.of(active));

        assertThat(sessionService.findActiveByRawId(raw)).isEqualTo(active);
    }

    @Test
    void touchByRawId_delegatesToPortWithHashedValue() {
        String raw = "abc";

        sessionService.touchByRawId(raw);

        verify(sessionSavePort, times(1)).touch(sessionService.hash(raw));
    }

    @Test
    void revokeByRawId_delegatesToPortWithHashedValue() {
        String raw = "abc";

        sessionService.revokeByRawId(raw);

        verify(sessionRevokePort, times(1)).revokeByHash(sessionService.hash(raw));
    }

    @Test
    void hash_isDeterministicAndHexEncoded() {
        assertThat(sessionService.hash("hello")).isEqualTo(sessionService.hash("hello"));
        assertThat(sessionService.hash("hello")).isNotEqualTo(sessionService.hash("world"));
        assertThat(sessionService.hash("hello")).matches("[0-9a-f]{64}");
    }
}
