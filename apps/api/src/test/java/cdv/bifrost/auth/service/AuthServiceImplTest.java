package cdv.bifrost.auth.service;

import cdv.bifrost.auth.exception.InvalidCredentialsException;
import cdv.bifrost.auth.exception.PasswordMismatchException;
import cdv.bifrost.auth.exception.UserAlreadyExist;
import cdv.bifrost.auth.exception.UserNotActiveException;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.auth.model.request.LoginRequest;
import cdv.bifrost.auth.model.request.RegisterRequest;
import cdv.bifrost.auth.port.UserReadPort;
import cdv.bifrost.auth.port.UserSavePort;
import cdv.bifrost.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserReadPort userReadPort;

    @Mock
    UserSavePort userSavePort;

    @Mock
    SessionService sessionService;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthServiceImpl authService;

    private static RegisterRequest validRegister() {
        RegisterRequest req = new RegisterRequest();
        req.setDisplayName("Test User");
        req.setEmail("user@example.com");
        req.setPassword("password123");
        req.setPasswordConfirmation("password123");
        return req;
    }

    private static LoginRequest validLogin() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("password123");
        return req;
    }

    @Test
    void register_succeeds_andHashesPassword() {
        RegisterRequest req = validRegister();
        when(userReadPort.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pw");
        when(userSavePort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userSavePort, times(1)).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getName()).isEqualTo("Test User");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed-pw");
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void register_throwsUserAlreadyExist_whenEmailTaken() {
        RegisterRequest req = validRegister();
        when(userReadPort.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(User.builder().id("u").build()));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExist.class);

        verify(userSavePort, never()).save(any());
    }

    @Test
    void register_throwsPasswordMismatch_whenConfirmationDiffers() {
        RegisterRequest req = validRegister();
        req.setPasswordConfirmation("differentpwd");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(PasswordMismatchException.class);

        verify(userReadPort, never()).findByEmailIgnoreCase(any());
        verify(userSavePort, never()).save(any());
    }

    @Test
    void login_succeeds_andCreatesSession() {
        LoginRequest req = validLogin();
        User user = User.builder()
                .id("u")
                .email("user@example.com")
                .passwordHash("hashed-pw")
                .status(UserStatus.ACTIVE)
                .build();
        when(userReadPort.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);
        when(sessionService.create(any(), any(), any())).thenReturn("raw-session");

        String raw = authService.login(req, "127.0.0.1", "UA");

        assertThat(raw).isEqualTo("raw-session");
        verify(sessionService, times(1)).create(user, "127.0.0.1", "UA");
    }

    @Test
    void login_throwsInvalidCredentials_whenUserMissing() {
        LoginRequest req = validLogin();
        when(userReadPort.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req, null, null))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsInvalidCredentials_whenPasswordDoesNotMatch() {
        LoginRequest req = validLogin();
        User user = User.builder()
                .id("u")
                .email("user@example.com")
                .passwordHash("hashed-pw")
                .status(UserStatus.ACTIVE)
                .build();
        when(userReadPort.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req, null, null))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsUserNotActive_whenStatusNotActive() {
        LoginRequest req = validLogin();
        User user = User.builder()
                .id("u")
                .email("user@example.com")
                .passwordHash("hashed-pw")
                .status(UserStatus.PENDING_VERIFICATION)
                .build();
        when(userReadPort.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(req, null, null))
                .isInstanceOf(UserNotActiveException.class);
    }
}
