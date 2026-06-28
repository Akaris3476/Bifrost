package cdv.bifrost.auth.service;

import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.User;

public interface SessionService {
    String create(User user, String ipAddress, String userAgent);

    AuthSession findActiveByRawId(String rawSessionId);

    void touchByRawId(String rawSessionId);

    void revokeByRawId(String rawSessionId);

    String hash(String rawSessionId);
}
