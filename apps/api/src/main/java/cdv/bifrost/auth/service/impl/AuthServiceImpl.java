package cdv.bifrost.auth.service.impl;

import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.request.RegisterRequest;
import cdv.bifrost.auth.port.UserReadPort;
import cdv.bifrost.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserReadPort userReadPort;

    @Override
    public boolean isUserExist(String email) {
        return userReadPort.findByEmailIgnoreCase(email).isPresent();
    }

    @Override
    public User registerUser(RegisterRequest registerRequest) {
        return null;
    }
}
