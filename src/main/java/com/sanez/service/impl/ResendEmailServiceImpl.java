package com.sanez.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.sanez.exception.EnvioEmailException;
import com.sanez.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementación de EmailService usando Resend para entorno de producción.
 */
@Service
@Profile("prod")
@Slf4j
public class ResendEmailServiceImpl implements EmailService {

    private final Resend resendClient;
    private final TemplateEngine templateEngine;
    private final String fromEmail;
    private final String fromName;
    private final String baseUrl;

    public ResendEmailServiceImpl(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.from.email}") String fromEmail,
            @Value("${resend.from.name}") String fromName,
            @Value("${app.base-url}") String baseUrl,
            TemplateEngine templateEngine) {

        this.resendClient = new Resend(apiKey);
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.baseUrl = baseUrl;
        this.templateEngine = templateEngine;

        log.info("ResendEmailService inicializado para producción");
        log.info("Remitente configurado: {} <{}>", fromName, fromEmail);
    }

    @Override
    @Retryable(
            retryFor = {ResendException.class, EnvioEmailException.class},
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 2000,      // 2 segundos de delay inicial
                    multiplier = 2.0,  // Exponencial: 2s, 4s, 8s
                    maxDelay = 10000   // Máximo 10 segundos entre reintentos
            )
    )
    public void enviarEmailVerificacion(String destinatario, String token) {
        log.info("Enviando email de verificación a: {}", destinatario);

        String asunto = "Verifica tu cuenta - Notas App";
        String linkVerificacion = baseUrl + "/verify.html?token=" + token;

        Context context = new Context();
        context.setVariable("linkVerificacion", linkVerificacion);

        String htmlContent = templateEngine.process("email/email-verificacion", context);

        //TODO extraer código repetitivo y agregar a un método privado
        try {
            CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                    .from(fromName + " <" + fromEmail + ">")
                    .to(destinatario)
                    .subject(asunto)
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resendClient.emails().send(emailOptions);
            log.info("Email de verificación enviado exitosamente. ID: {}", response.getId());

        } catch (ResendException e) {
            log.error("Error al enviar email de verificación a {}: {}", destinatario, e.getMessage());
            throw new EnvioEmailException("No se pudo enviar el email de verificación", e);
        }
    }

    @Override
    @Retryable(
            retryFor = {ResendException.class, EnvioEmailException.class},
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2.0,
                    maxDelay = 10000
            )
    )
    public void enviarEmailRecuperacionPassword(String destinatario, String token) {
        log.info("Enviando email de recuperación de contraseña a: {}", destinatario);

        String asunto = "Recuperación de contraseña - Notas App";
        String linkReset = baseUrl + "/reset-password.html?token=" + token;

        Context context = new Context();
        context.setVariable("linkReset", linkReset);

        String htmlContent = templateEngine.process("email/email-recuperacion", context);

        try {
            CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                    .from(fromName + " <" + fromEmail + ">")
                    .to(destinatario)
                    .subject(asunto)
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resendClient.emails().send(emailOptions);
            log.info("Email de recuperación enviado exitosamente. ID: {}", response.getId());

        } catch (ResendException e) {
            log.error("Error al enviar email de recuperación a {}: {}", destinatario, e.getMessage());
            throw new EnvioEmailException("No se pudo enviar el email de recuperación de contraseña", e);
        }
    }

    @Recover
    public void recover(EnvioEmailException e, String destinatario, String token) {
        log.error("Todos los reintentos fallaron al enviar email a {}. Error final: {}",
                destinatario, e.getMessage());

        throw new EnvioEmailException(
                "El servicio de email no está disponible temporalmente. Por favor, intenta más tarde.",
                e
        );
    }
}