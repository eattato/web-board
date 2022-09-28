package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Repository
@Slf4j
public class MailService {
    @Autowired
    private JavaMailSenderImpl mailSender;

    public void sendVerifyMessage(String email) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom("boardarc.community@gmail.com");
            helper.setTo(email);
            helper.setSubject("Boardarc - 이메일 인증");
            helper.setText("안녕하십니까? 하단 버튼을 눌러 이메일 인증해 Boardarc 회원가입을 완료하세요.\n" +
                    "만약 회원가입을 시도하지 않았다면 이 메일을 무시하세요.");
            mailSender.send(message);
        } catch (MessagingException e) {
            log.info(e.toString());
        }
    }
}
