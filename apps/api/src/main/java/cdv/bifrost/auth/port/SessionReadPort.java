package cdv.bifrost.auth.port;

import cdv.bifrost.auth.model.AuthSession;

import java.util.Optional;

public interface SessionReadPort {
    Optional<AuthSession> findByHash(String sessionHash);
}
