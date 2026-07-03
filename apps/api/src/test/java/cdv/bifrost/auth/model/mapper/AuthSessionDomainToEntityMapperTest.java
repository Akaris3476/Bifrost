package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSessionDomainToEntityMapperTest {

    private final AuthSessionDomainToEntityMapper mapper = Mappers.getMapper(AuthSessionDomainToEntityMapper.class);

    @Test
    void convertsLocalDateTimeToUtcOffsetDateTime_andIgnoresIdAndTimestamps() {
        LocalDateTime expires = LocalDateTime.of(2026, 1, 15, 12, 0, 0);
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        AuthSession session = AuthSession.builder()
                .id("ignored")
                .userId(userId.toString())
                .sessionHash("hash-1")
                .ipAddress("127.0.0.1")
                .userAgent("agent")
                .lastSeenAt(expires)
                .expiresAt(expires)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        AuthSessionEntity entity = mapper.map(session);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getSessionHash()).isEqualTo("hash-1");
        assertThat(entity.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(entity.getUserAgent()).isEqualTo("agent");
        assertThat(entity.getLastSeenAt()).isEqualTo(OffsetDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC));
        assertThat(entity.getExpiresAt()).isEqualTo(OffsetDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    @Test
    void mapsNullTimesToNull() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        AuthSession session = AuthSession.builder()
                .userId(userId.toString())
                .sessionHash("h")
                .build();

        AuthSessionEntity entity = mapper.map(session);

        assertThat(entity.getLastSeenAt()).isNull();
        assertThat(entity.getExpiresAt()).isNull();
        assertThat(entity.getRevokedAt()).isNull();
    }
}
