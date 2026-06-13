package com.sanez.service.impl;

import com.sanez.exception.EnvioEmailException;
import com.sanez.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Esta implementación usa Mailtrap como servicio de testing de emails.
 */
@Service
@Profile("dev")
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String baseUrl;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.baseUrl = baseUrl;
        log.info("EmailServiceImpl (dev) inicializado. app.base-url = {}", baseUrl);
    }

    @Override
    public void enviarEmailVerificacion(String destinatario, String token) {
        log.info("Enviando email de verificación a: {} ", destinatario);

        String asunto = "Verifica tu cuenta - Notas App";
        String linkVerificacion = baseUrl + "/verify.html?token=" + token;
        log.info("Link de verificación generado: {}", linkVerificacion);

        Context context = new Context();
        context.setVariable("linkVerificacion", linkVerificacion);

        String htmlContent = templateEngine.process("email/email-verificacion", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email de verificación enviado exitosamente a: {}", destinatario);

        } catch (MessagingException e) {
            log.error("Error al enviar email de verificación a {}: {}", destinatario, e.getMessage());
            throw new EnvioEmailException("No se pudo enviar el email de verificación", e);
        }
    }

    @Override
    public void enviarEmailRecuperacionPassword(String destinatario, String token) {
        log.info("Enviando email de recuperación de contraseña a: {} ", destinatario);

        String asunto = "Recuperación de contraseña - Notas App";
        String linkReset = baseUrl + "/reset-password.html?token=" + token;
        log.info("Link de recuperación generado: {}", linkReset);

        Context context = new Context();
        context.setVariable("linkReset", linkReset);

        String htmlContent = templateEngine.process("email/email-recuperacion", context);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email de recuperación enviado exitosamente a: {}", destinatario);

        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a {}: {}", destinatario, e.getMessage());
            throw new EnvioEmailException("No se pudo enviar el email de recuperación de contraseña", e);
        }
    }
}
