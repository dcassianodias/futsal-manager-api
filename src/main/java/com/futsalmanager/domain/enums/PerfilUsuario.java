package com.futsalmanager.domain.enums;

public enum PerfilUsuario {

    ADMIN("ROLE_ADMIN"),
    ATLETA("ROLE_ATLETA");

    private final String role;

    PerfilUsuario(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
