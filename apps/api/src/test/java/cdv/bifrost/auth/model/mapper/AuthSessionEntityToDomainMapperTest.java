package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSessionEntityToDomainMapperTest {

    private final AuthSessionEntityToDomainMapper mapper = Mappers.getMapper(AuthSessionEntityToDomainMapper.class);

    @Test
    void mapsEntityToDomain_andConvertsOffsetDateTimeToUtcLocalDateTime() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        OffsetDateTime created = OffsetDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneOffset.ofHours(3));
        AuthSessionEntity entity = AuthSessionEntity.builder()
                .id(sessionId)
                .userId(userId)
                .sessionHash("hash-1")
                .ipAddress("127.0.0.1")
                .userAgent("agent")
                .lastSeenAt(created)
                .expiresAt(created)
                .revokedAt(null)
                .createdAt(created)
                .updatedAt(created)
                .build();

        AuthSession domain = mapper.map(entity);

        assertThat(domain.getId()).isEqualTo(sessionId.toString());
        assertThat(domain.getUserId()).isEqualTo(userId.toString());
        assertThat(domain.getSessionHash()).isEqualTo("hash-1");
        assertThat(domain.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(domain.getUserAgent()).isEqualTo("agent");
        assertThat(domain.getLastSeenAt())
                .isEqualTo(created.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
    }

    @Test
    void ignoresCreatedUserAndUpdatedAt() {
        UUID userId = UUID.randomUUID();
        AuthSessionEntity entity = AuthSessionEntity.builder()
                .userId(userId)
                .sessionHash("h")
                .lastSeenAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        AuthSession domain = mapper.map(entity);

        assertThat(domain.getCreatedUser()).isNull();
    }
}
