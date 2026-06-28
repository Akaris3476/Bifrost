package cdv.bifrost.auth.controller;

import cdv.bifrost.BifrostApplication;
import cdv.bifrost.TestcontainersConfiguration;
import cdv.bifrost.auth.constant.SessionConstant;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.auth.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = BifrostApplication.class)
class AuthControllerIntegrationTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private void registerUser(String email, String displayName) throws Exception {
        String body = """
                {
                  "displayName": "%s",
                  "email": "%s",
                  "password": "password123",
                  "passwordConfirmation": "password123"
                }
                """.formatted(displayName, email);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private String loginBody(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    @Test
    void register_successful() throws Exception {
        String email = "new-user@example.com";
        registerUser(email, "New User");

        assertThat(userRepository.findByEmailIgnoreCase(email)).isPresent();
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getPasswordHash()).isNotNull().isNotEqualTo("password123");
    }

    @Test
    void register_duplicateEmail_returns4xx() throws Exception {
        registerUser("duplicate@example.com", "Dup");

        String body = """
                {
                  "displayName": "Dup2",
                  "email": "duplicate@example.com",
                  "password": "password123",
                  "passwordConfirmation": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void register_passwordMismatch_returns4xx() throws Exception {
        String body = """
                {
                  "displayName": "Mismatch",
                  "email": "mismatch@example.com",
                  "password": "password123",
                  "passwordConfirmation": "different123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());

        assertThat(userRepository.findByEmailIgnoreCase("mismatch@example.com")).isEmpty();
    }

    @Test
    void login_validCredentials_setsSecureCookie() throws Exception {
        String email = "login-ok@example.com";
        registerUser(email, "Login OK");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "password123")))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(SessionConstant.SESSION_COOKIE_NAME))
                .andExpect(cookie().httpOnly(SessionConstant.SESSION_COOKIE_NAME, true))
                .andExpect(cookie().secure(SessionConstant.SESSION_COOKIE_NAME, true))
                .andExpect(cookie().path(SessionConstant.SESSION_COOKIE_NAME, "/"))
                .andExpect(cookie().sameSite(SessionConstant.SESSION_COOKIE_NAME, "Lax"));
    }

    @Test
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        String email = "wrong-pw@example.com";
        registerUser(email, "Wrong PW");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withoutCookie_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withInvalidCookie_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .cookie(new Cookie(SessionConstant.SESSION_COOKIE_NAME, "bogus-value")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withValidCookie_returnsUser() throws Exception {
        String email = "me@example.com";
        registerUser(email, "Test User");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "password123")))
                .andExpect(status().isOk())
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie(SessionConstant.SESSION_COOKIE_NAME);
        assertThat(sessionCookie).isNotNull();

        mockMvc.perform(get("/auth/me")
                        .cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.displayName").value("Test User"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void logout_revokesSession_andClearsCookie() throws Exception {
        String email = "logout@example.com";
        registerUser(email, "Logout User");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "password123")))
                .andExpect(status().isOk())
                .andReturn();
        Cookie sessionCookie = loginResult.getResponse().getCookie(SessionConstant.SESSION_COOKIE_NAME);
        assertThat(sessionCookie).isNotNull();

        mockMvc.perform(post("/auth/logout")
                        .cookie(sessionCookie)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(SessionConstant.SESSION_COOKIE_NAME, 0));

        mockMvc.perform(get("/auth/me")
                        .cookie(sessionCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_withInvalidSessionCookie_stillReturnsOk_andClearsCookie() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie(SessionConstant.SESSION_COOKIE_NAME, "never-valid"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(SessionConstant.SESSION_COOKIE_NAME, 0));
    }

    @Test
    void loginResponse_doesNotLeakPasswordOrSessionInBody() throws Exception {
        String email = "no-leak@example.com";
        registerUser(email, "No Leak");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "password123")))
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
    void csrfProtectedEndpoint_withoutCsrfHeader_isRejected() throws Exception {
        String email = "csrf-test@example.com";
        registerUser(email, "CSRF Test");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, "password123")))
                .andExpect(status().isOk())
                .andReturn();
        Cookie sessionCookie = loginResult.getResponse().getCookie(SessionConstant.SESSION_COOKIE_NAME);
        assertThat(sessionCookie).isNotNull();

        mockMvc.perform(get("/workspaces/00000000-0000-0000-0000-000000000000/invitations")
                        .cookie(sessionCookie))
                .andExpect(status().isForbidden());
    }
}
