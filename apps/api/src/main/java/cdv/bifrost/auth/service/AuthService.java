package cdv.bifrost.auth.service;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.request.RegisterRequest;

public interface AuthService {
    boolean isUserExist(String email);

    User registerUser(RegisterRequest registerRequest);
}
