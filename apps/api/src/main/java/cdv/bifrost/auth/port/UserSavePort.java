package cdv.bifrost.auth.port;

import cdv.bifrost.auth.model.User;

public interface UserSavePort {
    User save(User user);
}
