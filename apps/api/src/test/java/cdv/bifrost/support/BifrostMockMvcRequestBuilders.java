package cdv.bifrost.support;

import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public final class BifrostMockMvcRequestBuilders {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private BifrostMockMvcRequestBuilders() {
    }

    public static MockHttpServletRequestBuilder get(String endpoint, Cookie sessionCookie) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(endpoint);
        if (sessionCookie != null) {
            builder.cookie(sessionCookie);
        }
        return builder;
    }

    public static MockHttpServletRequestBuilder post(String endpoint, Cookie sessionCookie, Object body) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(endpoint)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body));
        if (sessionCookie != null) {
            builder.cookie(sessionCookie);
        }
        return builder;
    }

    public static MockHttpServletRequestBuilder delete(String endpoint, Cookie sessionCookie) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete(endpoint).with(csrf());
        if (sessionCookie != null) {
            builder.cookie(sessionCookie);
        }
        return builder;
    }

    private static String toJson(Object body) {
        if (body == null) {
            return "";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(body);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot serialize request body", e);
        }
    }
}
