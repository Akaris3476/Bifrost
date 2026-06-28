package cdv.bifrost.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SessionAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthUserPrincipal principal;
    private final String credentials;

    public SessionAuthenticationToken(
            AuthUserPrincipal principal,
            String credentials,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    public String sessionHash() {
        return principal != null ? principal.sessionHash() : null;
    }
}
