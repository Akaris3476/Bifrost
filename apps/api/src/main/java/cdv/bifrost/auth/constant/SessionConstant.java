package cdv.bifrost.auth.constant;

import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor
public final class SessionConstant {
    public static final String SESSION_COOKIE_NAME = "BIFROST_SESSION";
    public static final Duration SESSION_TTL = Duration.ofDays(7);
    public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";
}
