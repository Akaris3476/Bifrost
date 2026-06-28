package cdv.bifrost.auth.port.adapter;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.model.mapper.UserDomainToEntityMapper;
import cdv.bifrost.auth.model.mapper.UserEntityToDomainMapper;
import cdv.bifrost.auth.port.UserReadPort;
import cdv.bifrost.auth.port.UserSavePort;
import cdv.bifrost.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class UserAdapter implements UserReadPort, UserSavePort {

    private final UserRepository userRepository;
    private final UserEntityToDomainMapper userEntityToDomainMapper;
    private final UserDomainToEntityMapper userDomainToEntityMapper;

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        Optional<UserEntity> userEntity = this.userRepository.findByEmailIgnoreCase(email);
        // INFO: this is S.map(xx:map) because there is Optional/Stream/List collection and map for each element
        return userEntity.map(userEntityToDomainMapper::map);
    }

    @Override
    public User save(final User user) {
        final UserEntity userEntity = userDomainToEntityMapper.map(user);
        final UserEntity savedUserEntity = this.userRepository.save(userEntity);
        // INFO: mapper.map because there is only one element
        return userEntityToDomainMapper.map(savedUserEntity);
    }
}
