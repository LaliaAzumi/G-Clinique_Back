package com.erp.clinique.dto;

/**
 * Requête de login pour l'API frontend (React/Next.js)
 */
public class LoginApiRequest {
    private String username;
    private String password;
    
    // Constructeurs
    public LoginApiRequest() {}
    
    public LoginApiRequest(String username, String password) {
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
