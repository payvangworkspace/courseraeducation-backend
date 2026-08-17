package com.pv.couseae.services;

import com.pv.couseae.entities.User;
import org.thymeleaf.context.Context;

public interface EmailServices{
    void sendWithTemplate(String to, String subject, String templateName, Context context);
    void sendEmail(String to, String subject, String body);
    void sendOtp(String to, String fullName, String userName, int otp);
    void sendOnboarding(User user);
    void sendMerchantVerification(User user);
}
