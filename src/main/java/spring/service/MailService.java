package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Repository;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Repository
@Slf4j
public class MailService {
    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    private Random random = new Random();

    private String generateRandomCode() {
        String result = "";
        for (int ind = 1; ind <= 6; ind++) {
            int textOrInt = random.nextInt(2);
            if (textOrInt == 0) {
                result += (char)(random.nextInt(26) + 97);
            } else {
                result += (char)(random.nextInt(10) + 48);
            }
        }
        return result;
    }

    public void sendVerifyMessage(String email) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom("boardarc@naver.com");
            helper.setTo(email);
            helper.setSubject("Boardarc - 이메일 인증");

            Context context = new Context();
            context.setVariable("name", "temp");
            context.setVariable("code", generateRandomCode());
            String htmlText = templateEngine.process("mail/verify", context);
            helper.setText(htmlText, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.info(e.toString());
        }
    }
}
