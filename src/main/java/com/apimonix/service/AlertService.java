package com.apimonix.service;

import com.apimonix.model.Endpoint;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    @Value("${apimonix.resend.api-key}")
    private String resendApiKey;

    @Value("${apimonix.resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    public void sendDownAlert(Endpoint endpoint){
        log.info("🔥 sendDownAlert CALLED for endpoint {}", endpoint.getId());
        String subject = "🔴 DOWN: " + endpoint.getName() + " is not responding";

        String html = """
                <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                                    <div style="background: #ef4444; padding: 20px; border-radius: 8px 8px 0 0;">
                                        <h2 style="color: white; margin: 0;">🔴 Service Down</h2>
                                    </div>
                                    <div style="background: #f9fafb; padding: 24px; border-radius: 0 0 8px 8px; border: 1px solid #e5e7eb;">
                                        <p style="font-size: 16px; color: #111827;">
                                            <strong>%s</strong> is not responding.
                                        </p>
                                        <div style="background: white; padding: 16px; border-radius: 6px; border: 1px solid #e5e7eb; margin: 16px 0;">
                                            <p style="margin: 0; color: #6b7280; font-size: 14px;">Endpoint URL</p>
                                            <p style="margin: 4px 0 0; color: #111827; font-weight: 500;">%s</p>
                                        </div>
                                        <p style="color: #6b7280; font-size: 14px;">
                                            We'll notify you when it recovers.
                                        </p>
                                        <p style="color: #6b7280; font-size: 12px; margin-top: 24px; border-top: 1px solid #e5e7eb; padding-top: 16px;">
                                            Apimonix - API Monitoring
                                        </p>
                                    </div>
                                </div>
                """.formatted(endpoint.getName(), endpoint.getUrl());

        sendEmail(
                endpoint.getUser().getEmail(),
                subject,
                html
        );
    }

    public void sendRecoveryAlert(Endpoint endpoint){
        String subject = "✅ RECOVERED: " + endpoint.getName()+" is back online";
        String html = """
                <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                                    <div style="background: #10b981; padding: 20px; border-radius: 8px 8px 0 0;">
                                        <h2 style="color: white; margin: 0;">✅ Service Recovered</h2>
                                    </div>
                                    <div style="background: #f9fafb; padding: 24px; border-radius: 0 0 8px 8px; border: 1px solid #e5e7eb;">
                                        <p style="font-size: 16px; color: #111827;">
                                            <strong>%s</strong> is back online.
                                        </p>
                                        <div style="background: white; padding: 16px; border-radius: 6px; border: 1px solid #e5e7eb; margin: 16px 0;">
                                            <p style="margin: 0; color: #6b7280; font-size: 14px;">Endpoint URL</p>
                                            <p style="margin: 4px 0 0; color: #111827; font-weight: 500;">%s</p>
                                        </div>
                                        <p style="color: #6b7280; font-size: 14px;">
                                            Your service is responding normally again.
                                        </p>
                                        <p style="color: #6b7280; font-size: 12px; margin-top: 24px; border-top: 1px solid #e5e7eb; padding-top: 16px;">
                                            Apimonix - API Monitoring
                                        </p>
                                    </div>
                                </div>
                """.formatted(endpoint.getName(), endpoint.getUrl());
        sendEmail(
                endpoint.getUser().getEmail(),
                subject,
                html
        );
    }

    private void sendEmail(String to, String subject, String html){
        try{
            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(to),
                    "subject", subject,
                    "html", html
            );

            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Alert email sent to [{}] - subject: {}", to, subject);
        } catch (Exception e){
            log.error("Failed to send alert email to [{}]: {}", to, e);
        }
    }

}
