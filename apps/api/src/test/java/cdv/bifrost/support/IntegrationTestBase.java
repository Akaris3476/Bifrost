package cdv.bifrost.support;

import cdv.bifrost.BifrostApplication;
import cdv.bifrost.TestcontainersConfiguration;
import cdv.bifrost.auth.port.*;
import cdv.bifrost.auth.repository.AuthSessionRepository;
import cdv.bifrost.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Import({TestcontainersConfiguration.class, LogTracker.class})
@SpringBootTest(classes = BifrostApplication.class, properties = "spring.jpa.hibernate.ddl-auto=none")
public abstract class IntegrationTestBase {

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AuthSessionRepository authSessionRepository;

    @Autowired
    protected UserReadPort userReadPort;

    @Autowired
    protected UserSavePort userSavePort;

    @Autowired
    protected SessionReadPort sessionReadPort;

    @Autowired
    protected SessionSavePort sessionSavePort;

    @Autowired
    protected SessionRevokePort sessionRevokePort;

    @Autowired
    protected LogTracker logTracker;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        cleanupAuthTables();
    }

    private void cleanupAuthTables() {
        jdbcTemplate.execute("TRUNCATE TABLE auth_sessions CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
    }

    protected ResultActions performWith(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
                                        ResultMatcher... matchers) throws Exception {
        ResultActions actions = mockMvc.perform(builder);
        for (ResultMatcher matcher : matchers) {
            actions = actions.andExpect(matcher);
        }
        return actions;
    }
}
