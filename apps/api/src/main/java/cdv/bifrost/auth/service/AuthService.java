package cdv.bifrost.auth.service;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.request.LoginRequest;
import cdv.bifrost.auth.model.request.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest registerRequest);

    String login(LoginRequest request, String ipAddress, String userAgent);

    User me(String rawSessionId);

    void logout(String rawSessionId);
}
