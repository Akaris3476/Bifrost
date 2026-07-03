package cdv.bifrost.support;

import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.CookieResultMatchers;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public final class BifrostMockResultMatchers {

    private BifrostMockResultMatchers() {
    }

    public static StatusAssertions status() {
        return new StatusAssertions();
    }

    public static ResponseAssertions response() {
        return new ResponseAssertions(null);
    }

    public static ResponseAssertions response(String jsonPath) {
        return new ResponseAssertions(jsonPath);
    }

    public static CookieAssertions cookie(String name) {
        return new CookieAssertions(name);
    }

    public static ResultMatcher message(String expected) {
        return jsonPath("$.message").value(expected);
    }

    public static ResultMatcher errorCode(String expected) {
        return jsonPath("$.errorCode").value(expected);
    }

    public static ResultMatcher success(boolean expected) {
        return jsonPath("$.success").value(expected);
    }

    public static final class StatusAssertions {

        public ResultMatcher isOk() {
            return MockMvcResultMatchers.status().isOk();
        }

        public ResultMatcher isCreated() {
            return MockMvcResultMatchers.status().isCreated();
        }

        public ResultMatcher isNoContent() {
            return MockMvcResultMatchers.status().isNoContent();
        }

        public ResultMatcher isBadRequest() {
            return MockMvcResultMatchers.status().isBadRequest();
        }

        public ResultMatcher isUnauthorized() {
            return MockMvcResultMatchers.status().isUnauthorized();
        }

        public ResultMatcher isForbidden() {
            return MockMvcResultMatchers.status().isForbidden();
        }

        public ResultMatcher isNotFound() {
            return MockMvcResultMatchers.status().isNotFound();
        }

        public ResultMatcher isConflict() {
            return MockMvcResultMatchers.status().isConflict();
        }
    }

    public static final class ResponseAssertions {

        private final String path;

        private ResponseAssertions(String path) {
            this.path = path;
        }

        public ResultMatcher isNotEmpty() {
            return matcher().exists();
        }

        public ResultMatcher isEmpty() {
            return matcher().doesNotExist();
        }

        public ResultMatcher doesNotExist() {
            return matcher().doesNotExist();
        }

        public ResultMatcher value(String expected) {
            return matcher().value(expected);
        }

        public ResultMatcher exists() {
            return matcher().exists();
        }

        private JsonPathResultMatchers matcher() {
            return path == null
                    ? jsonPath("$.data")
                    : jsonPath(path.startsWith("$") ? path : "$.data." + path);
        }
    }

    public static final class CookieAssertions {

        private final String name;
        private final CookieResultMatchers delegate;

        private CookieAssertions(String name) {
            this.name = name;
            this.delegate = MockMvcResultMatchers.cookie();
        }

        public ResultMatcher exists() {
            return delegate.exists(name);
        }

        public ResultMatcher isHttpOnly() {
            return delegate.httpOnly(name, true);
        }

        public ResultMatcher isSecure() {
            return delegate.secure(name, true);
        }

        public ResultMatcher hasPath(String expected) {
            return delegate.path(name, expected);
        }

        public ResultMatcher hasSameSite(String expected) {
            return delegate.sameSite(name, expected);
        }

        public ResultMatcher hasMaxAge(int expected) {
            return delegate.maxAge(name, expected);
        }
    }
}
