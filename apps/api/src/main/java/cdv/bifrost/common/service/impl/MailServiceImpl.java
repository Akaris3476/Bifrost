package cdv.bifrost.common.service.impl;

import cdv.bifrost.common.configuration.MailConfiguration;
import cdv.bifrost.common.model.Mail;
import cdv.bifrost.common.service.MailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final Resend resend;
    private final TemplateEngine templateEngine;
    private final MailConfiguration mailConfiguration;

    @Async
    @Override
    public void sendEmail(Mail mail) {
        String html = renderTemplate(mail);
        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(mailConfiguration.getFrom())
                .to(mail.getTo().toArray(new String[0]))
                .subject(mail.getSubject())
                .html(html)
                .build();

        try {
            resend.emails().send(options);
        } catch (ResendException e) {
            throw new RuntimeException("Couldn't send mail", e);
        }
    }

    private String renderTemplate(Mail mail) {
        Context context = new Context();
        mail.getParameters().forEach(context::setVariable);
        return templateEngine.process(mail.getTemplate(), context);
    }
}
