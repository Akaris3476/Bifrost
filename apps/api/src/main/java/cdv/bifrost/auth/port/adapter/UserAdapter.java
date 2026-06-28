package cdv.bifrost.auth.port.adapter;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.model.mapper.UserEntityToDomainMapper;
import cdv.bifrost.auth.port.UserReadPort;
import cdv.bifrost.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
class UserAdapter implements UserReadPort {

    private final UserRepository userRepository;
    private final UserEntityToDomainMapper userEntityToDomainMapper;

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        Optional<UserEntity> userEntity = this.userRepository.findByEmailIgnoreCase(email);
        return userEntity.map(userEntityToDomainMapper::map);
    }
}
