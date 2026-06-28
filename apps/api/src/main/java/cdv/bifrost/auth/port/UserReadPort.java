package cdv.bifrost.auth.port;

import cdv.bifrost.auth.model.User;

import java.util.Optional;

public interface UserReadPort {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findById(String id);
}
