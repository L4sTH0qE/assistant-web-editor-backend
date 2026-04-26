package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/// Service for sending verification codes to email.
@Service
@RequiredArgsConstructor
@Log4j2
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Код подтверждения: Помощник редакторов сайтов подразделений НИУ ВШЭ");

            message.setText("Здравствуйте!\n\n" +
                    "Ваш код для регистрации в системе «Помощник редакторов сайтов подразделений НИУ ВШЭ»: " + code + "\n\n" +
                    "Код действителен в течение 10 минут.\n" +
                    "Если вы не запрашивали этот код, просто проигнорируйте письмо.\n\n" +
                    "С уважением,\nСлужба технической поддержки");

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send verification code", e);
        }
    }
}
