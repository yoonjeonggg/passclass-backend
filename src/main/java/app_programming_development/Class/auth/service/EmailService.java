package app_programming_development.Class.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@passclass.com}")
    private String from;

    public void sendVerificationCode(String to, String code) {
        if (mailSender == null) {
            log.warn("메일 서버 미설정 - 인증 코드 발송 생략. email={}, code={}", to, code);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("[PassClass] 이메일 인증 코드");
            message.setText("PassClass 이메일 인증 코드: " + code + "\n\n유효 시간: 5분");
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Failed to send email to {}. Verification code: {}", to, code);
        }
    }
}
