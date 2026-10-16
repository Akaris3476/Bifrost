package cdv.bifrost.common.configuration;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfiguration {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from;

    @Bean
    public Resend resend() {
        return new Resend(apiKey);
    }

    public String getFrom() {
        return from;
    }
}
