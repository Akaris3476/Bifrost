package cdv.bifrost.auth.service.impl;

import cdv.bifrost.auth.exception.UserAlreadyExist;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.auth.model.request.RegisterRequest;
import cdv.bifrost.auth.port.UserReadPort;
import cdv.bifrost.auth.port.UserSavePort;
import cdv.bifrost.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserReadPort userReadPort;
    private final UserSavePort userSavePort;

    @Override
    public boolean isUserExist(String email) {
        return userReadPort.findByEmailIgnoreCase(email).isPresent();
    }

    public void register(RegisterRequest request) {
        if (!this.isUserExist(request.getEmail())) {
            throw new UserAlreadyExist(request.getEmail());
        }

        // builder
        User user = User.builder()
                .name(request.getDisplayName())
                .status(UserStatus.ACTIVE)
                .build();

        this.userSavePort.save(user);
        // TODO: send a couple of email ( confirmation, and welcome email)
        // INFO: use resend api
    }
}
