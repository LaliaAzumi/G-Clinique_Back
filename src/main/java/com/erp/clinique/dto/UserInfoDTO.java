package com.erp.clinique.dto;

/**
 * DTO pour les informations utilisateur
 * Retourné au microservice FastAPI
 */
public class UserInfoDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean firstLogin; // Indique si c'est la première connexion de l'utilisateur
    
    // Constructeurs
    public UserInfoDTO() {}
    
    public UserInfoDTO(Long id, String username, String email, String role, Boolean firstLogin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.firstLogin = firstLogin;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    public Boolean getFirstLogin() {
        return firstLogin;
    }
    public void setFirstLogin(Boolean firstLogin) {
        this.firstLogin = firstLogin;
    }
}
