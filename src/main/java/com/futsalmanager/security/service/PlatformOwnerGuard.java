package com.futsalmanager.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("platformOwnerGuard")
public class PlatformOwnerGuard {

    private final String ownerEmail;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public PlatformOwnerGuard(
            @Value("${app.platform-owner-email:}") String ownerEmail,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.ownerEmail = ownerEmail;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public boolean isOwner() {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            return false;
        }
        String emailLogado = authenticatedUserProvider.getUsuarioAutenticado().getEmail();
        return ownerEmail.trim().equalsIgnoreCase(emailLogado == null ? null : emailLogado.trim());
    }

    public String debug() {
        String emailLogado = authenticatedUserProvider.getUsuarioAutenticado().getEmail();
        return "ownerEmailConfigurado=[" + ownerEmail + "] (tamanho=" + (ownerEmail == null ? 0 : ownerEmail.length()) + ") | "
                + "emailLogado=[" + emailLogado + "] (tamanho=" + (emailLogado == null ? 0 : emailLogado.length()) + ") | "
                + "souDono=" + isOwner();
    }
}
