package cdv.bifrost.auth.repository;

import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_thenFindById() {
        UserEntity entity = UserEntity.builder()
                .email("repo-find@bifrost.local")
                .displayName("Repo User")
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity saved = userRepository.save(entity);
        assertThat(saved.getId()).isNotNull();

        var found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("repo-find@bifrost.local");
    }

    @Test
    void findByEmailIgnoreCase_matchesCaseInsensitively() {
        UserEntity entity = UserEntity.builder()
                .email("Lower@Bifrost.Local")
                .displayName("u")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(entity);

        assertThat(userRepository.findByEmailIgnoreCase("lower@bifrost.local")).isPresent();
        assertThat(userRepository.findByEmailIgnoreCase("LOWER@BIFROST.LOCAL")).isPresent();
        assertThat(userRepository.findByEmailIgnoreCase("LoWeR@bIfRoSt.lOcAl")).isPresent();
    }

    @Test
    void findByEmailIgnoreCase_returnsEmptyForMissing() {
        assertThat(userRepository.findByEmailIgnoreCase("ghost@bifrost.local")).isEmpty();
    }

    @Test
    void uniqueConstraintOnEmail_throwsOnDuplicate() {
        userRepository.save(UserEntity.builder()
                .email("dup@bifrost.local")
                .displayName("first")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build());

        assertThatThrownBy(() -> userRepository.saveAndFlush(UserEntity.builder()
                .email("dup@bifrost.local")
                .displayName("second")
                .passwordHash("h")
                .status(UserStatus.ACTIVE)
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
