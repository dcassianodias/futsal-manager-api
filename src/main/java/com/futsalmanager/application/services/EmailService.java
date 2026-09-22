package com.futsalmanager.application.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Envia e-mail via API HTTP do Brevo (não SMTP): o Railway bloqueia conexões
 * SMTP de saída (porta 587) por padrão anti-abuso, então a integração precisa
 * ser HTTPS.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String remetente;
    private final String frontendUrl;

    public EmailService(@Value("${app.email.brevo-api-key:}") String apiKey,
                         @Value("${app.email.remetente:}") String remetente,
                         @Value("${app.frontend-url}") String frontendUrl) {
        this.apiKey = apiKey;
        this.remetente = remetente;
        this.frontendUrl = frontendUrl;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .build();
    }

    /**
     * Falha de envio não deve derrubar o fluxo de "esqueci minha senha" (a resposta
     * pro usuário é sempre genérica, ver AuthService.esqueciSenha) — só loga.
     */
    public void enviarEmailRedefinicaoSenha(String destinatario, String nome, String token) {
        String link = frontendUrl + "/redefinir-senha?token=" + token;

        String corpo = """
                <div style="font-family: Arial, sans-serif; background-color: #0a0d10; padding: 32px; color: #e5e7eb;">
                  <div style="max-width: 480px; margin: 0 auto; background-color: #12161b; border-radius: 12px; padding: 32px; border: 1px solid #1f2937;">
                    <h1 style="color: #00db4d; font-size: 22px; margin-top: 0;">Jogaí</h1>
                    <p>Olá, %s!</p>
                    <p>Recebemos uma solicitação para redefinir a senha da sua conta. Se não foi você, pode ignorar este e-mail.</p>
                    <p style="text-align: center; margin: 32px 0;">
                      <a href="%s" style="background-color: #00db4d; color: #0a0d10; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: bold; display: inline-block;">Redefinir senha</a>
                    </p>
                    <p style="font-size: 13px; color: #9ca3af;">Este link expira em 30 minutos. Se o botão não funcionar, copie e cole este endereço no navegador:</p>
                    <p style="font-size: 13px; color: #9ca3af; word-break: break-all;">%s</p>
                  </div>
                </div>
                """.formatted(nome, link, link);

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .header("accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sender", Map.of("name", "Jogaí", "email", remetente),
                            "to", List.of(Map.of("email", destinatario, "name", nome)),
                            "subject", "Redefinição de senha - Jogaí",
                            "htmlContent", corpo
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de redefinição de senha para {}", destinatario, e);
        }
    }

    /**
     * Falha de envio não deve derrubar o lançamento do pagamento — só loga, igual ao
     * e-mail de redefinição de senha.
     */
    public void enviarEmailNovaCobranca(String destinatario, String nome, String nomeTime, BigDecimal valor,
                                         String referencia) {
        String link = frontendUrl + "/pagamentos";
        String valorFormatado = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor);

        String corpo = """
                <div style="font-family: Arial, sans-serif; background-color: #0a0d10; padding: 32px; color: #e5e7eb;">
                  <div style="max-width: 480px; margin: 0 auto; background-color: #12161b; border-radius: 12px; padding: 32px; border: 1px solid #1f2937;">
                    <h1 style="color: #00db4d; font-size: 22px; margin-top: 0;">Jogaí</h1>
                    <p>Olá, %s!</p>
                    <p>Uma nova cobrança foi lançada para você no time <strong>%s</strong>:</p>
                    <p style="text-align: center; margin: 24px 0;">
                      <span style="font-size: 26px; font-weight: bold; color: #00db4d;">%s</span><br>
                      <span style="font-size: 13px; color: #9ca3af;">%s</span>
                    </p>
                    <p style="text-align: center; margin: 32px 0;">
                      <a href="%s" style="background-color: #00db4d; color: #0a0d10; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: bold; display: inline-block;">Ver meus pagamentos</a>
                    </p>
                  </div>
                </div>
                """.formatted(nome, nomeTime, valorFormatado, referencia, link);

        try {
            restClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .header("accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "sender", Map.of("name", "Jogaí", "email", remetente),
                            "to", List.of(Map.of("email", destinatario, "name", nome)),
                            "subject", "Nova cobrança lançada - Jogaí",
                            "htmlContent", corpo
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de nova cobrança para {}", destinatario, e);
        }
    }
}
