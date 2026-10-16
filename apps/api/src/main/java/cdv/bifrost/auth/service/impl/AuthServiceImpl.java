package cdv.bifrost.auth.service.impl;

import cdv.bifrost.auth.exception.InvalidCredentialsException;
import cdv.bifrost.auth.exception.PasswordMismatchException;
import cdv.bifrost.auth.exception.UserAlreadyExist;
import cdv.bifrost.auth.exception.UserNotActiveException;
import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.auth.model.request.LoginRequest;
import cdv.bifrost.auth.model.request.RegisterRequest;
import cdv.bifrost.auth.port.UserReadPort;
import cdv.bifrost.auth.port.UserSavePort;
import cdv.bifrost.auth.service.AuthService;
import cdv.bifrost.auth.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserReadPort userReadPort;
    private final UserSavePort userSavePort;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMismatchException();
        }
        if (userReadPort.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new UserAlreadyExist(request.getEmail());
        }

        // TODO: check invitation code to assign user to a team.

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .name(request.getDisplayName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .build();

        userSavePort.save(user);
    }

    @Override
    @Transactional
    public String login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userReadPort.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        // passwordHash nullable for admin-created users pending activation
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(user.getEmail());
        }

        return sessionService.create(user, ipAddress, userAgent);
    }

    @Override
    @Transactional
    public User me(String rawSessionId) {
        AuthSession session = sessionService.findActiveByRawId(rawSessionId);
        if (session == null) {
            throw new InvalidCredentialsException();
        }

        sessionService.touchByRawId(rawSessionId);

        return userReadPort.findById(session.getUserId())
                .orElseThrow(InvalidCredentialsException::new);
    }

    @Override
    @Transactional
    public void logout(String rawSessionId) {
        sessionService.revokeByRawId(rawSessionId);
    }
}
