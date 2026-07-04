package cdv.bifrost.common.service;

import cdv.bifrost.common.model.Mail;

public interface MailService {
    void sendEmail(Mail mail);
}
