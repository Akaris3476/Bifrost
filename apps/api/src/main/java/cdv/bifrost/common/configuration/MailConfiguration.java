package cdv.bifrost.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfiguration {
    @Value("${spring.mail.from-mail}")
    public String fromMail;
}
