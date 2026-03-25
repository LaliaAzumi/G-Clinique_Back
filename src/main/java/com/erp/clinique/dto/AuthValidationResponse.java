package com.erp.clinique.dto;

/**
 * DTO pour la réponse de validation d'authentification
 * Retourné au microservice FastAPI
 */
public class AuthValidationResponse {
    private boolean valid;
    private String message;
    private Long userId;
    private String username;
    private String role;
    
    // Constructeurs
    public AuthValidationResponse() {}
    
    public AuthValidationResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
    
    public AuthValidationResponse(boolean valid, String message, Long userId, String username, String role) {
        this.valid = valid;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
    
    // Getters et Setters
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}
