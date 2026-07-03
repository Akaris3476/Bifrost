package cdv.bifrost.support;

public final class BifrostTestData {

    private BifrostTestData() {
    }

    public static final String DEFAULT_EMAIL = "auth-test@bifrost.local";
    public static final String DEFAULT_DISPLAY_NAME = "Test User";
    public static final String DEFAULT_PASSWORD = "Password123";
    public static final String DEFAULT_PASSWORD_CONFIRMATION = "Password123";

    public static final String OTHER_EMAIL = "second-user@bifrost.local";
    public static final String SHORT_PASSWORD = "short";
    public static final String LONG_PASSWORD = "P4ssword".repeat(10);

    public static final String SESSION_COOKIE = "BIFROST_SESSION";
    public static final String CSRF_HEADER = "X-XSRF-TOKEN";
}
