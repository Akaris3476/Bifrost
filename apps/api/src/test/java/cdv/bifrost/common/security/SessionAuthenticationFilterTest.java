package cdv.bifrost.common.security;

import cdv.bifrost.auth.constant.SessionConstant;
import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.model.User;
import cdv.bifrost.auth.model.enums.UserStatus;
import cdv.bifrost.auth.port.UserReadPort;
import cdv.bifrost.auth.service.SessionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SessionAuthenticationFilterTest {

    private final SessionService sessionService = mock(SessionService.class);
    private final UserReadPort userReadPort = mock(UserReadPort.class);
    private final SessionAuthenticationFilter filter = new SessionAuthenticationFilter(sessionService, userReadPort);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noCookie_leavesContextEmpty_andContinuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void validCookie_populatesSecurityContext() throws Exception {
        User user = User.builder()
                .id("user-id-1")
                .email("u@b.c")
                .status(UserStatus.ACTIVE)
                .build();
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("user-id-1")
                .sessionHash("h")
                .build();

        when(sessionService.findActiveByRawId("raw-id")).thenReturn(session);
        when(userReadPort.findById("user-id-1")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(SessionConstant.SESSION_COOKIE_NAME, "raw-id"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(AuthUserPrincipal.class);
        AuthUserPrincipal principal = (AuthUserPrincipal) auth.getPrincipal();
        assertThat(principal.userId()).isEqualTo("user-id-1");
        assertThat(principal.email()).isEqualTo("u@b.c");
        assertThat(principal.sessionHash()).isEqualTo("raw-id");
        assertThat(auth.getAuthorities()).extracting(Object::toString).contains("ROLE_USER");
    }

    @Test
    void unknownSessionHash_leavesContextEmpty() throws Exception {
        when(sessionService.findActiveByRawId("unknown")).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(SessionConstant.SESSION_COOKIE_NAME, "unknown"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userReadPort, never()).findById(any());
    }

    @Test
    void sessionFoundButUserMissing_leavesContextEmpty() throws Exception {
        AuthSession session = AuthSession.builder()
                .id("s")
                .userId("ghost")
                .sessionHash("h")
                .build();

        when(sessionService.findActiveByRawId("raw-id")).thenReturn(session);
        when(userReadPort.findById("ghost")).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(SessionConstant.SESSION_COOKIE_NAME, "raw-id"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void multipleCookies_onlySessionCookieIsRead() throws Exception {
        User user = User.builder().id("u1").email("u@b.c").status(UserStatus.ACTIVE).build();
        AuthSession session = AuthSession.builder()
                .id("s").userId("u1").sessionHash("h").build();

        when(sessionService.findActiveByRawId("raw-id")).thenReturn(session);
        when(userReadPort.findById("u1")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("other-cookie", "other-value"),
                new Cookie(SessionConstant.SESSION_COOKIE_NAME, "raw-id"),
                new Cookie("yet-another", "x")
        );
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(sessionService).findActiveByRawId(eq("raw-id"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void existingAuthentication_isReplaced() throws Exception {
        SecurityContextHolder.setContext(new SecurityContextImpl());
        SecurityContextHolder.getContext().setAuthentication(
                new SessionAuthenticationToken(
                        new AuthUserPrincipal("old", "old@x", "old-raw"),
                        null,
                        java.util.List.of()
                )
        );

        AuthSession session = AuthSession.builder()
                .id("s").userId("new-id").sessionHash("new-h").build();
        User user = User.builder().id("new-id").email("n@b.c").status(UserStatus.ACTIVE).build();

        when(sessionService.findActiveByRawId("raw-id")).thenReturn(session);
        when(userReadPort.findById("new-id")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(SessionConstant.SESSION_COOKIE_NAME, "raw-id"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        AuthUserPrincipal principal = (AuthUserPrincipal)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(principal.userId()).isEqualTo("new-id");
    }
}
