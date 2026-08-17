package com.pv.couseae.notification;

import com.pv.couseae.entities.Documents;
import com.pv.couseae.entities.EmailMaster;
import com.pv.couseae.entities.PaymentLinks;
import com.pv.couseae.entities.User;
import com.pv.couseae.repos.EmailMasterRepo;
import com.pv.couseae.services.EmailServices;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@AllArgsConstructor
public class Notifications {
    private EmailServices emailServices;
    private final EmailMasterRepo emailMasterRepo;
    private final JavaMailSenderImpl sender = new JavaMailSenderImpl();
    private final TemplateEngine templateEngine;

    @Async
    public void sendAdminOnboard(User newUser) {
        String subject = "Admin Onboarding";
        Context context = new Context();
        context.setVariable("adminName", newUser.getUserId());

        this.emailServices.sendWithTemplate(newUser.getUserId(), subject, "Onboarding/Admin", context);
    }

    @Async
    public void LoadMoneyNotificationEmail(String mailTemplate,String toEmail,String amount) {

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", toEmail);
        vars.put("amount", amount);
        //vars.put("date", "2025-12-10");
        // Send email using SMTP
        EmailMaster emailMaster =emailMasterRepo.findByEmailCode(mailTemplate);
        if (emailMaster == null) {
            throw new RuntimeException("Email config not found for code: " + mailTemplate);
        }

        // Configure SMTP dynamically
        sender.setHost(emailMaster.getSmtpHost());
        sender.setPort(emailMaster.getSmtpPort());
        sender.setUsername(emailMaster.getSmtpUser());
        sender.setPassword(emailMaster.getSmtpPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", emailMaster.isUseSsl());

        // Prepare Thymeleaf template (String)
        Context context = new Context();
        context.setVariables(vars);

        String htmlContent = templateEngine.process(
                emailMaster.getBodyTemplate(),  // template name OR inline template
                context
        );

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(emailMaster.getFromEmail());
            helper.setTo(toEmail);

            if (emailMaster.getCcEmail() != null && !emailMaster.getCcEmail().isBlank()) {
                helper.setCc(emailMaster.getCcEmail());
            }

            helper.setSubject(emailMaster.getSubject());
            helper.setText(htmlContent, true); // enable HTML

            sender.send(message);

            System.out.println("Mail sent successfully!");

        } catch (Exception e) {
            throw new RuntimeException("Error sending mail: " + e.getMessage(), e);
        }


    }
    @Async
    public void sendOnboardingMerchant(User user) {
        String subject = "Merchant Onboarding";
        Context context = new Context();
        context.setVariable("merchantName",user.getFullName());
        context.setVariable("merchantId",user.getUserId());
        this.emailServices.sendWithTemplate(user.getUserId(), subject, "Onboarding/Merchant", context);
    }

    public void sendOnboardingSubAdmin(User user) {
        String subject = "Subadmin Onboarding";
        Context context = new Context();
        context.setVariable("subadminName",user.getFullName());
        context.setVariable("subadminId",user.getUserId());
        this.emailServices.sendWithTemplate(user.getUserId(), subject, "Onboarding/Subadmin", context);
    }

    public void sendOnboardingReseller(User user) {
        String subject = "Reseller Onboarding";
        Context context = new Context();
        context.setVariable("resellerName",user.getFullName());
        context.setVariable("resellerId",user.getUserId());
        this.emailServices.sendWithTemplate(user.getUserId(), subject, "Onboarding/Reseller", context);
    }

    public void sendOnboardingSubMerchant(User user) {
        String subject = "Sub-Merchant Onboarding";
        Context context = new Context();
        context.setVariable("subMerchantName",user.getFullName());
        context.setVariable("subMerchantId",user.getUserId());
        this.emailServices.sendWithTemplate(user.getUserId(), subject, "Onboarding/Submerchant", context);
    }

    public void sendDocumentVerify(Documents documents) {
        String subject = "Document Verified Successfully";
        Context context = new Context();
        context.setVariable("fullName",documents.getUser().getFullName());
        context.setVariable("documentName",documents.getDocumentFileName());
        this.emailServices.sendWithTemplate(documents.getUser().getFullName(), subject, "UserStatus/DocumentVerification", context);
    }



    public void sendPaymentLinkEmail(PaymentLinks generatedLink) {
        String subject = "Payment Link";
        Context context = new Context();
        context.setVariable("fullName",generatedLink.getCustomerName());
        context.setVariable("paymentLink",generatedLink.getPaymentLinkUrl());
        this.emailServices.sendWithTemplate(generatedLink.getNotifyEmail(), subject, "PaymentLinkGenerate/OnGeneration", context);
    }
}
