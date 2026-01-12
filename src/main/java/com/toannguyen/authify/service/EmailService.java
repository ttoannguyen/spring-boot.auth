package com.toannguyen.authify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    JavaMailSender javaMailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    @NonFinal
    String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Welcome to Our Platform");
        message.setText("Hello" + name + ",\n\nThanks for registering with us!\n\nRegards, \nAuthify Team.");
        javaMailSender.send(message);
    }

    public void sendResetOtpEmail(String email, String otp) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(email);
        mailMessage.setSubject("Password Reset Otp");
        mailMessage.setText("Your OTP for resetting your password is " + otp
                + ". Use this OTP to proceed  with resetting your password.");
        javaMailSender.send(mailMessage);

    }

}
