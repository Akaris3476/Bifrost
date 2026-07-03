package cdv.bifrost.auth.model.mapper;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.model.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class UserDomainToEntityMapperTest {

    private final UserDomainToEntityMapper mapper = Mappers.getMapper(UserDomainToEntityMapper.class);

    @Test
    void mapsNameToDisplayName_andIgnoresIdAndTimestamps() {
        User domain = User.builder()
                .id("ignored-id")
                .email("user@example.com")
                .name("Display Name")
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .createdUser("creator")
                .build();

        UserEntity entity = mapper.map(domain);

        assertThat(entity.getDisplayName()).isEqualTo("Display Name");
        assertThat(entity.getEmail()).isEqualTo("user@example.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hash");
        assertThat(entity.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(entity.getId()).isNull();
    }

    @Test
    void mapsNullNameToNullDisplayName() {
        User domain = User.builder()
                .email("a@b.c")
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity entity = mapper.map(domain);

        assertThat(entity.getDisplayName()).isNull();
    }
}
