package cdv.bifrost.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;

@TestConfiguration(proxyBeanMethods = false)
public class LogTracker {

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Logger root;

    @PostConstruct
    void attach() {
        root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        appender.start();
        root.addAppender(appender);
    }

    @PreDestroy
    void detach() {
        if (root != null) {
            root.detachAppender(appender);
        }
        appender.stop();
    }

    public Optional<String> findMessage(Level level, String exact) {
        return appender.list.stream()
                .filter(event -> event.getLevel().equals(level))
                .map(ILoggingEvent::getFormattedMessage)
                .filter(exact::equals)
                .findFirst();
    }

    public Optional<String> findMessageContaining(Level level, String fragment) {
        return appender.list.stream()
                .filter(event -> event.getLevel().equals(level))
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message != null && message.contains(fragment))
                .findFirst();
    }

    public List<ILoggingEvent> events() {
        return List.copyOf(appender.list);
    }

    @Bean
    LogTracker logTracker() {
        return this;
    }
}
