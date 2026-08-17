package com.pv.couseae.services;

import com.pv.couseae.entities.User;
import com.pv.couseae.security.SystemConfigurations;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
public class EmailServiceImplement implements EmailServices{

    private JavaMailSender mailSender;
    private  TemplateEngine templateEngine;

    @Async
    private void send(String to, String subject, String templateName, Context context) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        try {
            helper.setTo(to);
            helper.setFrom(SystemConfigurations.getSenderMail());
            helper.setSubject(subject);
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {// Handle exception
        }
    }


    @Override
    @Async
    public void sendWithTemplate(String to, String subject, String templateName, Context context) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        try {
            helper.setTo(to);
            helper.setFrom(SystemConfigurations.getSenderMail());
            helper.setSubject(subject);
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {// Handle exception
        }
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom(SystemConfigurations.getSenderMail());
        mailSender.send(message);
    }
    @Override
    public void sendOtp(String to, String fullName, String userName, int otp) {
        String subject = "Sigmassist account verification";
        Context context = new Context();
        context.setVariable("fullName",fullName);
        context.setVariable("userName",userName);
        context.setVariable("otp",otp);
        send(to, subject, "email_otp", context);
    }

    @Override
    public void sendOnboarding(User user) {
        String subject = "Merchant Onboarding";
        Context context = new Context();
        context.setVariable("merchantName",user.getFullName());
        context.setVariable("merchantId",user.getUserId());

        send(user.getUserId(), subject, "merchantCreate", context);
    }

    @Override
    public void sendMerchantVerification(User user) {
        String subject = "Account verification";
        Context context = new Context();
        context.setVariable("userName",user.getFullName());

        send(user.getUserId(), subject, "merchantCreate", context);
    }

}
