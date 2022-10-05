package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Repository;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import spring.dto.AccountDataDTO;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Repository
@Slf4j
public class MailService {
    @Autowired
    private AccountService accountService;

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendVerifyMessage(AccountDataDTO userData) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom("boardarc@naver.com");
            helper.setTo(userData.getEmail());
            helper.setSubject("Boardarc - 이메일 인증");

            Context context = new Context();
            context.setVariable("name", userData.getNickname());
            context.setVariable("code", userData.getVcode());
            String htmlText = templateEngine.process("mail/verify", context);
            helper.setText(htmlText, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.info(e.toString());
        }
    }

    public void sendResetMessage(AccountDataDTO userData) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom("boardarc@naver.com");
            helper.setTo(userData.getEmail());
            helper.setSubject("Boardarc - 비밀번호 변경 요청");

            Context context = new Context();
            context.setVariable("name", userData.getNickname());
            context.setVariable("code", userData.getVcode());
            String htmlText = templateEngine.process("mail/reset", context);
            helper.setText(htmlText, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.info(e.toString());
        }
    }
}
