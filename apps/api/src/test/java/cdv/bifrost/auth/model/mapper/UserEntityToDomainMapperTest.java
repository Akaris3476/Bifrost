package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.model.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityToDomainMapperTest {

    private final UserEntityToDomainMapper mapper = Mappers.getMapper(UserEntityToDomainMapper.class);

    @Test
    void mapsDisplayNameToName_andConvertsOffsetDateTimeToUtcLocalDateTime() {
        OffsetDateTime created = OffsetDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneOffset.ofHours(3));
        UserEntity entity = UserEntity.builder()
                .id(java.util.UUID.randomUUID())
                .email("user@example.com")
                .displayName("Display Name")
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .createdAt(created)
                .updatedAt(created)
                .build();

        User domain = mapper.map(entity);

        assertThat(domain.getName()).isEqualTo("Display Name");
        assertThat(domain.getEmail()).isEqualTo("user@example.com");
        assertThat(domain.getPasswordHash()).isEqualTo("hash");
        assertThat(domain.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(domain.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 15, 9, 0, 0));
    }

    @Test
    void preservesUtcOffsetDateTime() {
        OffsetDateTime created = OffsetDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC);
        UserEntity entity = UserEntity.builder()
                .id(java.util.UUID.randomUUID())
                .email("a@b.c")
                .status(UserStatus.ACTIVE)
                .createdAt(created)
                .updatedAt(created)
                .build();

        User domain = mapper.map(entity);

        assertThat(domain.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 15, 12, 0, 0));
    }
}
