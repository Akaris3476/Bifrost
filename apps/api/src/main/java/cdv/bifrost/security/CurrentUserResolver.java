package cdv.bifrost.security;

import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public UserEntity resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthUserPrincipal authPrincipal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unsupported principal");
        }

        UUID userId;
        try {
            userId = UUID.fromString(authPrincipal.userId());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user id");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user does not exist"));
    }
}
