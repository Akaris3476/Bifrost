package cdv.bifrost.auth.port;

import cdv.bifrost.auth.model.AuthSession;

public interface SessionSavePort {
    AuthSession save(AuthSession session);

    void touch(String sessionHash);
}
