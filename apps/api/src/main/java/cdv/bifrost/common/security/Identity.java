package cdv.bifrost.common.security;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Identity {

    public boolean isAuthenticated() {
        AuthUserPrincipal principal = tryGetPrincipal();
        return principal != null;
    }

    public String getId() {
        return requirePrincipal().userId();
    }

    public String getEmail() {
        return requirePrincipal().email();
    }

    public String getSessionHash() {
        return requirePrincipal().sessionHash();
    }

    private static AuthUserPrincipal tryGetPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUserPrincipal authPrincipal) {
            return authPrincipal;
        }
        return null;
    }

    private static AuthUserPrincipal requirePrincipal() {
        AuthUserPrincipal principal = tryGetPrincipal();
        if (principal == null) {
            throw new IllegalStateException("No authenticated user in current request");
        }
        return principal;
    }
}
