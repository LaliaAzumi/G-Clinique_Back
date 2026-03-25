package com.erp.clinique.dto;

/**
 * DTO pour la validation des identifiants utilisateur
 * Utilisé par le microservice FastAPI d'authentification
 */
public class AuthValidationRequest {
    private String username;
    private String password;
    
    // Constructeurs
    public AuthValidationRequest() {}
    
    public AuthValidationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getters et Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
