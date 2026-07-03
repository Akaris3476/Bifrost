package cdv.bifrost.auth.controller;

import cdv.bifrost.auth.model.entity.AuthSessionEntity;
import cdv.bifrost.auth.model.entity.UserEntity;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.auth.support.LoginRequestBuilder;
import cdv.bifrost.auth.support.RegisterRequestBuilder;
import cdv.bifrost.support.BifrostMockMvcRequestBuilders;
import cdv.bifrost.support.BifrostMockResultMatchers;
import cdv.bifrost.support.BifrostTestData;
import cdv.bifrost.support.IntegrationTestBase;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static cdv.bifrost.support.BifrostMockResultMatchers.cookie;
import static cdv.bifrost.support.BifrostMockResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

class AuthSessionEndToEndTest extends IntegrationTestBase {

    private static final String LOGIN_ENDPOINT = "/auth/login";
    private static final String LOGOUT_ENDPOINT = "/auth/logout";
    private static final String ME_ENDPOINT = "/auth/me";
    private static final String REGISTER_ENDPOINT = "/auth/register";

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void registerUser(String email) throws Exception {
        var request = RegisterRequestBuilder.valid()
                .withEmail(email)
                .withDisplayName("User " + email)
                .build();
        performWith(
                BifrostMockMvcRequestBuilders.post(REGISTER_ENDPOINT, null, request),
                status().isOk()
        );
    }

    private Cookie login(String email, String password) throws Exception {
        var result = mockMvc.perform(BifrostMockMvcRequestBuilders.post(LOGIN_ENDPOINT, null,
                        LoginRequestBuilder.valid()
                                .withEmail(email)
                                .withPassword(password)
                                .build()))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(BifrostTestData.SESSION_COOKIE);
    }

    private UserEntity seedUser(String email, UserStatus status, String rawPassword) {
        UserEntity entity = UserEntity.builder()
                .email(email)
                .displayName("Seeded " + email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .status(status)
                .build();
        return userRepository.save(entity);
    }

    private Cookie cookieForSeededSession(UserEntity user, OffsetDateTime expiresAt, OffsetDateTime revokedAt) {
        String knownRaw = "test-raw-" + UUID.randomUUID();
        AuthSessionEntity entity = AuthSessionEntity.builder()
                .userId(user.getId())
                .sessionHash(sha256Hex(knownRaw))
                .ipAddress("127.0.0.1")
                .userAgent("JUnit")
                .lastSeenAt(OffsetDateTime.now())
                .expiresAt(expiresAt)
                .revokedAt(revokedAt)
                .build();
        authSessionRepository.save(entity);
        return new Cookie(BifrostTestData.SESSION_COOKIE, knownRaw);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    // =================== LOGIN ===================

    @Test
    void givenRegisteredUser_whenLoggingInWithCorrectCredentials_thenReturnHttp200AndSessionCookie() throws Exception {

        // Initialize
        String email = "login-ok@bifrost.local";
        registerUser(email);

        // Given
        var request = LoginRequestBuilder.valid()
                .withEmail(email)
                .withPassword(BifrostTestData.DEFAULT_PASSWORD)
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGIN_ENDPOINT, null, request),
                status().isOk(),
                BifrostMockResultMatchers.success(true),
                cookie(BifrostTestData.SESSION_COOKIE).exists(),
                cookie(BifrostTestData.SESSION_COOKIE).isHttpOnly(),
                cookie(BifrostTestData.SESSION_COOKIE).isSecure(),
                cookie(BifrostTestData.SESSION_COOKIE).hasPath("/"),
                cookie(BifrostTestData.SESSION_COOKIE).hasSameSite("Lax")
        );
    }

    @Test
    void givenLoginRequestWithWrongPassword_whenLoggingIn_thenReturnHttp401Unauthorized() throws Exception {

        // Initialize
        String email = "wrong-pw@bifrost.local";
        registerUser(email);

        // Given
        var request = LoginRequestBuilder.valid()
                .withEmail(email)
                .withPassword("WrongPassword1")
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGIN_ENDPOINT, null, request),
                status().isUnauthorized(),
                BifrostMockResultMatchers.success(false),
                BifrostMockResultMatchers.errorCode("INVALID_CREDENTIALS")
        );
    }

    @Test
    void givenLoginRequestWithEmailInDifferentCase_whenLoggingIn_thenReturnHttp200AndSessionCookie() throws Exception {

        // Initialize
        String email = "case-test@bifrost.local";
        registerUser(email);

        // Given
        var request = LoginRequestBuilder.valid()
                .withEmail(email.toUpperCase())
                .withPassword(BifrostTestData.DEFAULT_PASSWORD)
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGIN_ENDPOINT, null, request),
                status().isOk(),
                cookie(BifrostTestData.SESSION_COOKIE).exists()
        );
    }

    @Test
    void givenLoginRequestForNonexistentUser_whenLoggingIn_thenReturnHttp401Unauthorized() throws Exception {

        // Given
        var request = LoginRequestBuilder.valid()
                .withEmail("ghost@bifrost.local")
                .withPassword(BifrostTestData.DEFAULT_PASSWORD)
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGIN_ENDPOINT, null, request),
                status().isUnauthorized(),
                BifrostMockResultMatchers.errorCode("INVALID_CREDENTIALS")
        );
    }

    @Test
    void givenLoginResponse_whenLoggedIn_thenResponseBodyDoesNotLeakPasswordOrSessionValue() throws Exception {

        // Initialize
        String email = "no-leak@bifrost.local";
        registerUser(email);

        // Given
        var request = LoginRequestBuilder.valid()
                .withEmail(email)
                .withPassword(BifrostTestData.DEFAULT_PASSWORD)
                .build();

        // Then
        var result = mockMvc.perform(BifrostMockMvcRequestBuilders.post(LOGIN_ENDPOINT, null, request))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("password");
        assertThat(body).doesNotContain("passwordHash");

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("SameSite=Lax");
        assertThat(setCookie).contains("Path=/");
    }

    @Test
    void givenLoginRequestForDisabledUser_whenLoggingIn_thenReturnHttp403Forbidden() throws Exception {

        // Initialize
        String email = "disabled@bifrost.local";
        UserEntity user = seedUser(email, UserStatus.DISABLED, BifrostTestData.DEFAULT_PASSWORD);

        // Given
        var request = LoginRequestBuilder.valid()
                .withEmail(email)
                .withPassword(BifrostTestData.DEFAULT_PASSWORD)
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGIN_ENDPOINT, null, request),
                status().isForbidden(),
                BifrostMockResultMatchers.errorCode("USER_NOT_ACTIVE")
        );

        // Verify
        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void givenLoginRequestForPendingUser_whenLoggingIn_thenReturnHttp403Forbidden() throws Exception {

        // Initialize
        String email = "pending@bifrost.local";
        seedUser(email, UserStatus.PENDING_VERIFICATION, BifrostTestData.DEFAULT_PASSWORD);

        // Given
        var request = LoginRequestBuilder.valid()
                .withEmail(email)
                .withPassword(BifrostTestData.DEFAULT_PASSWORD)
                .build();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGIN_ENDPOINT, null, request),
                status().isForbidden(),
                BifrostMockResultMatchers.errorCode("USER_NOT_ACTIVE")
        );
    }

    // =================== ME ===================

    @Test
    void givenMeRequestWithoutCookie_whenCalled_thenReturnHttp401Unauthorized() throws Exception {

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, null),
                status().isUnauthorized()
        );
    }

    @Test
    void givenMeRequestWithInvalidSessionCookie_whenCalled_thenReturnHttp401Unauthorized() throws Exception {

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT,
                        new Cookie(BifrostTestData.SESSION_COOKIE, "bogus-value")),
                status().isUnauthorized()
        );
    }

    @Test
    void givenValidSessionCookie_whenMeCalled_thenReturnHttp200AndUserInfo() throws Exception {

        // Initialize
        String email = "me@bifrost.local";
        registerUser(email);
        Cookie session = login(email, BifrostTestData.DEFAULT_PASSWORD);

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, session),
                status().isOk(),
                BifrostMockResultMatchers.success(true),
                BifrostMockResultMatchers.response("email").value(email),
                BifrostMockResultMatchers.response("displayName").value("User " + email),
                BifrostMockResultMatchers.response("status").value("ACTIVE")
        );
    }

    @Test
    void givenValidSessionCookie_whenMeCalled_thenLastSeenAtIsUpdated() throws Exception {

        // Initialize
        String email = "last-seen@bifrost.local";
        registerUser(email);
        Cookie session = login(email, BifrostTestData.DEFAULT_PASSWORD);

        // Read initial state (the row was just created with last_seen_at = now)
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        var initialSessions = authSessionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(user.getId()))
                .toList();
        assertThat(initialSessions).hasSize(1);
        var initialLastSeen = initialSessions.get(0).getLastSeenAt();
        // Small delay to ensure timestamp strictly changes
        Thread.sleep(20);

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, session),
                status().isOk()
        );

        // Verify
        var afterSessions = authSessionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(user.getId()))
                .toList();
        assertThat(afterSessions).hasSize(1);
        assertThat(afterSessions.get(0).getLastSeenAt())
                .isAfter(initialLastSeen);
    }

    @Test
    void givenExpiredSession_whenMeCalled_thenReturnHttp401Unauthorized() throws Exception {

        // Initialize
        String email = "expired@bifrost.local";
        UserEntity user = seedUser(email, UserStatus.ACTIVE, BifrostTestData.DEFAULT_PASSWORD);
        Cookie cookie = cookieForSeededSession(user, OffsetDateTime.now().minusHours(1), null);

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, cookie),
                status().isUnauthorized()
        );
    }

    @Test
    void givenRevokedSession_whenMeCalled_thenReturnHttp401Unauthorized() throws Exception {

        // Initialize
        String email = "revoked@bifrost.local";
        UserEntity user = seedUser(email, UserStatus.ACTIVE, BifrostTestData.DEFAULT_PASSWORD);
        Cookie cookie = cookieForSeededSession(user,
                OffsetDateTime.now().plusHours(1),
                OffsetDateTime.now().minusMinutes(5));

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, cookie),
                status().isUnauthorized()
        );
    }

    @Test
    void givenSessionForUserThatWasDeleted_whenMeCalled_thenReturnHttp401Unauthorized() throws Exception {

        // Initialize — create a user, log in, then delete the user (CASCADE removes sessions)
        String email = "deleted-user@bifrost.local";
        registerUser(email);
        Cookie session = login(email, BifrostTestData.DEFAULT_PASSWORD);
        assertThat(session).isNotNull();

        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        jdbcTemplate.execute("DELETE FROM users WHERE id = '" + user.getId() + "'");

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, session),
                status().isUnauthorized()
        );
    }

    // =================== LOGOUT ===================

    @Test
    void givenValidSessionCookie_whenLoggingOut_thenReturnHttp200AndClearCookie() throws Exception {

        // Initialize
        String email = "logout@bifrost.local";
        registerUser(email);
        Cookie session = login(email, BifrostTestData.DEFAULT_PASSWORD);
        assertThat(session).isNotNull();

        // Then
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGOUT_ENDPOINT, session, null),
                status().isOk(),
                cookie(BifrostTestData.SESSION_COOKIE).hasMaxAge(0)
        );

        // Verify — DB row is revoked
        var sessions = authSessionRepository.findAll();
        assertThat(sessions).isNotEmpty();
        assertThat(sessions).allMatch(s -> s.getRevokedAt() != null);

        // And the session no longer authenticates
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, session),
                status().isUnauthorized()
        );
    }

    @Test
    void givenInvalidSessionCookie_whenLoggingOut_thenReturnHttp401() throws Exception {

        // Given
        Cookie bogus = new Cookie(BifrostTestData.SESSION_COOKIE, "never-valid");

        // Then — Spring Security rejects unauthenticated logout before the controller runs
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGOUT_ENDPOINT, bogus, null),
                status().isUnauthorized()
        );
    }

    @Test
    void givenTwoActiveSessions_whenLoggingOutOne_thenOtherRemainsValid() throws Exception {

        // Initialize
        String email = "two-sessions@bifrost.local";
        registerUser(email);
        Cookie sessionA = login(email, BifrostTestData.DEFAULT_PASSWORD);
        Cookie sessionB = login(email, BifrostTestData.DEFAULT_PASSWORD);
        assertThat(sessionA).isNotNull();
        assertThat(sessionB).isNotNull();

        // Then — logout A
        performWith(
                BifrostMockMvcRequestBuilders.post(LOGOUT_ENDPOINT, sessionA, null),
                status().isOk()
        );

        // Verify
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, sessionA),
                status().isUnauthorized()
        );
        performWith(
                BifrostMockMvcRequestBuilders.get(ME_ENDPOINT, sessionB),
                status().isOk()
        );
    }

    // =================== COOKIE / SESSION HASH PERSISTENCE ===================

    @Test
    void givenSessionHashPersisted_whenLoggedIn_thenHashIsSha256HexAndNotEqualToRawCookie() throws Exception {

        // Initialize
        String email = "hash-shape@bifrost.local";
        registerUser(email);
        Cookie sessionCookie = login(email, BifrostTestData.DEFAULT_PASSWORD);
        assertThat(sessionCookie).isNotNull();
        String rawCookie = sessionCookie.getValue();

        // Verify — find the matching row
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        Optional<AuthSessionEntity> matching = authSessionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(user.getId()))
                .findFirst();
        assertThat(matching).isPresent();
        AuthSessionEntity entity = matching.get();

        assertThat(entity.getSessionHash()).matches("[0-9a-f]{64}");
        assertThat(entity.getSessionHash()).isNotEqualTo(rawCookie);
    }
}
