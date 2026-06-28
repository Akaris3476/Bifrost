package cdv.bifrost.auth.service;

import cdv.bifrost.auth.model.request.RegisterRequest;

public interface AuthService {
    boolean isUserExist(String email);

    void register(RegisterRequest registerRequest);
}
