package cdv.bifrost.auth.port.adapter;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserAdapterTest extends IntegrationTestBase {

    @Test
    void save_persistsAndReturnsUserWithGeneratedId() {
        User user = User.builder()
                .email("adapter-save@bifrost.local")
                .name("Adapter Save")
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userSavePort.save(user);

        assertThat(saved.getId()).isNotBlank();
        assertThat(userRepository.findById(UUID.fromString(saved.getId()))).isPresent();
    }

    @Test
    void findByEmailIgnoreCase_returnsMappedDomainOrEmpty() {
        userSavePort.save(User.builder()
                .email("MixedCase@Bifrost.Local")
                .name("n")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());

        Optional<User> hit = userReadPort.findByEmailIgnoreCase("mixedcase@bifrost.local");
        assertThat(hit).isPresent();
        assertThat(hit.get().getEmail()).isEqualTo("MixedCase@Bifrost.Local");
        assertThat(hit.get().getName()).isEqualTo("n");

        assertThat(userReadPort.findByEmailIgnoreCase("nope@bifrost.local")).isEmpty();
    }

    @Test
    void findById_acceptsUuidString() {
        User saved = userSavePort.save(User.builder()
                .email("adapter-byid@bifrost.local")
                .name("n")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());

        Optional<User> hit = userReadPort.findById(saved.getId());
        assertThat(hit).isPresent();
        assertThat(hit.get().getEmail()).isEqualTo("adapter-byid@bifrost.local");

        assertThat(userReadPort.findById(UUID.randomUUID().toString())).isEmpty();
    }
}
