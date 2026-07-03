package cdv.bifrost.common.security;

import cdv.bifrost.auth.constant.SessionConstant;
import cdv.bifrost.auth.model.AuthSession;
import cdv.bifrost.auth.port.UserReadPort;
import cdv.bifrost.auth.service.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROLE_USER = "ROLE_USER";

    private final SessionService sessionService;
    private final UserReadPort userReadPort;

    public SessionAuthenticationFilter(
            SessionService sessionService,
            UserReadPort userReadPort
    ) {
        this.sessionService = sessionService;
        this.userReadPort = userReadPort;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        extractSessionCookie(request).ifPresent(rawSessionId -> {
            AuthSession session = sessionService.findActiveByRawId(rawSessionId);
            if (session == null) {
                return;
            }
            userReadPort.findById(session.getUserId()).ifPresent(user -> {
                AuthUserPrincipal principal = new AuthUserPrincipal(
                        user.getId(),
                        user.getEmail(),
                        rawSessionId
                );
                SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                ctx.setAuthentication(new SessionAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority(ROLE_USER))
                ));
                SecurityContextHolder.setContext(ctx);
                SecurityContextHolder.setDeferredContext(() -> ctx);
            });
        });

        filterChain.doFilter(request, response);
    }

    private static Optional<String> extractSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (SessionConstant.SESSION_COOKIE_NAME.equals(cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
