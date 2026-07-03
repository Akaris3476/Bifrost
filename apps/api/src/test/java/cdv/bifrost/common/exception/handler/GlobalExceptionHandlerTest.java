package cdv.bifrost.common.exception.handler;

import cdv.bifrost.auth.exception.InvalidCredentialsException;
import cdv.bifrost.auth.exception.PasswordMismatchException;
import cdv.bifrost.auth.exception.UserAlreadyExist;
import cdv.bifrost.auth.exception.UserNotActiveException;
import cdv.bifrost.common.model.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUserAlreadyExist_returns409() {
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExist(
                new UserAlreadyExist("dup@x"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("USER_ALREADY_EXISTS");
        assertThat(response.getBody().getMessage()).contains("dup@x");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleInvalidCredentials_returns401() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(
                new InvalidCredentialsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
    }

    @Test
    void handlePasswordMismatch_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handlePasswordMismatch(
                new PasswordMismatchException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("PASSWORD_MISMATCH");
        assertThat(response.getBody().isSuccess()).isFalse();
    }

    @Test
    void handleUserNotActive_returns403() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotActive(
                new UserNotActiveException("disabled@x"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getErrorCode()).isEqualTo("USER_NOT_ACTIVE");
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("disabled@x");
    }
}
