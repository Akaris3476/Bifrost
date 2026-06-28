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
import java.util.UUID;

@Component
@RequiredArgsConstructor
class UserAdapter implements UserReadPort, UserSavePort {

    private final UserRepository userRepository;
    private final UserDomainToEntityMapper userDomainToEntityMapper;
    private final UserEntityToDomainMapper userEntityToDomainMapper;

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(userEntityToDomainMapper::map);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(UUID.fromString(id))
                .map(userEntityToDomainMapper::map);
    }

    @Override
    public User save(User user) {
        UserEntity entity = userDomainToEntityMapper.map(user);
        UserEntity saved = userRepository.save(entity);
        return userEntityToDomainMapper.map(saved);
    }
}
