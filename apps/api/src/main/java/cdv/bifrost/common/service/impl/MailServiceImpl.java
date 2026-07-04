package cdv.bifrost.common.service.impl;

import cdv.bifrost.common.configuration.MailConfiguration;
import cdv.bifrost.common.model.Mail;
import cdv.bifrost.common.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailConfiguration mailConfiguration;

    @Async
    @Override
    public void sendEmail(Mail mail) {
        try {
            MimeMessage message = prepareMimeMessage(mail);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Couldn't send mail", e);
        }
    }

    private MimeMessage prepareMimeMessage(Mail mail) throws MessagingException {
        Context context = new Context();

        mail.getParameters().forEach(context::setVariable);

        String html = templateEngine.process(mail.getTemplate(), context);

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                "UTF-8"
        );

        helper.setFrom(mailConfiguration.fromMail);
        helper.setTo(mail.getTo().toArray(String[]::new));
        helper.setSubject(mail.getSubject());
        helper.setText(html, true);

        return message;
    }
}
