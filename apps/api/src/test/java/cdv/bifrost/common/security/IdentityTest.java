package cdv.bifrost.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentityTest {

    private final Identity identity = new Identity();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isAuthenticated_returnsFalseWhenNoPrincipal() {
        assertThat(identity.isAuthenticated()).isFalse();
    }

    @Test
    void getId_throwsWhenNotAuthenticated() {
        assertThatThrownBy(() -> identity.getId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No authenticated user");
    }

    @Test
    void getEmail_throwsWhenNotAuthenticated() {
        assertThatThrownBy(() -> identity.getEmail())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getSessionHash_throwsWhenNotAuthenticated() {
        assertThatThrownBy(() -> identity.getSessionHash())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsPrincipalValues_whenAuthenticated() {
        SecurityContextHolder.setContext(new SecurityContextImpl());
        SecurityContextHolder.getContext().setAuthentication(
                new SessionAuthenticationToken(
                        new AuthUserPrincipal("user-1", "user@x", "hash-1"),
                        null,
                        List.of()
                )
        );

        assertThat(identity.isAuthenticated()).isTrue();
        assertThat(identity.getId()).isEqualTo("user-1");
        assertThat(identity.getEmail()).isEqualTo("user@x");
        assertThat(identity.getSessionHash()).isEqualTo("hash-1");
    }

    @Test
    void nonAuthUserPrincipalPrincipalType_yieldsNotAuthenticated() {
        SecurityContextHolder.setContext(new SecurityContextImpl());
        Authentication auth = new Authentication() {
            @Override
            public String getName() {
                return "x";
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) {
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public Object getPrincipal() {
                return "not-an-AuthUserPrincipal";
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public Object getDetails() {
                return null;
            }
        };
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(identity.isAuthenticated()).isFalse();
    }
}
