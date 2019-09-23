package greencity.service.impl;

import greencity.constant.AppConstant;
import greencity.entity.Place;
import greencity.entity.User;
import greencity.entity.enums.PlaceStatus;
import greencity.service.EmailService;
import java.util.HashMap;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * {@inheritDoc}
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${client.address}")
    private String clientLink;

    /**
     * Constructor.
     *
     * @param javaMailSender {@link JavaMailSender} - use it for sending submits to users email
     * @param templateEngine - TemplateEngine to manege email templates
     */
    public EmailServiceImpl(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * {@inheritDoc}
     *
     * @author Nazar Vladyka
     */
    @Override
    public void sendChangePlaceStatusEmail(Place updatable, PlaceStatus status) {
        Map<String, Object> model = new HashMap<>();
        model.put("placeName", updatable.getName());
        model.put("status", status.toString().toLowerCase());
        model.put("user", updatable.getAuthor());
        model.put("clientLink", clientLink);

        String template = createEmailTemplate(model, "email-change-place-status");
        sendEmail(updatable.getAuthor(), "GreenCity contributors", template);
    }

    private void sendEmail(User receiver, String subject, String text) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        try {
            mimeMessageHelper.setTo(receiver.getEmail());
            mimeMessageHelper.setSubject(subject);
            mimeMessage.setContent(text, AppConstant.EMAIL_CONTENT_TYPE);
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }

        new Thread(() -> {
            javaMailSender.send(mimeMessage);
        }).start();
    }

    private String createEmailTemplate(Map<String, Object> vars, String templateName) {
        Context context = new Context();
        context.setVariables(vars);

        return templateEngine.process("email/" + templateName, context);
    }
}
