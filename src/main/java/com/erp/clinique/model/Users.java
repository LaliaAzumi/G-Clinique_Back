package com.erp.clinique.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "users") // "user" est réservé en MySQL, donc on met "users"
public class Users {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, unique = true)
	@NotNull(message = "username obligatoire")
	@NotBlank(message = "Nom non vide")
	private String username;
	
	@Column(nullable = false, unique = false)
	@NotNull(message = "email obligatoire")
	@NotBlank(message = "email non vide")
	private String email;
	
	private String mdp;
	private String role;
	
	// Champ pour savoir si c'est la première connexion
    private boolean firstLogin = true;
	
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

    public String getMdp() { 
    	return mdp; 
    }
    public void setMdp(String mdp) { 
    	this.mdp = mdp; 
    }

    public String getRole() { 
    	return role;
    }
    public void setRole(String role) { 
    	this.role = role; 
    } 
    
    public boolean isFirstLogin() { 
    	return firstLogin; 
    }
    public void setFirstLogin(boolean firstLogin) { 
    	this.firstLogin = firstLogin; 
    }
    //constructeurs
    public Users() {}
    
    public Users(String username, String email, String mdp, String role) {
    	this.username = username;
    	this.email = email;
    	this.mdp = mdp;
    	this.role = role;
    	this.firstLogin = true; // par défaut
    }
	
    
	
	

}
