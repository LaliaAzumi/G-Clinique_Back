package com.erp.clinique.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") // "user" est réservé en MySQL, donc on met "users"
public abstract class User {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String username;
	private String mdp;
	private String role;
	
	public Long getId() { 
		return id; 
	}
    public void setId(Long id) { 
    	this.id = id; 
    }

    public String getusername() { 
    	return username; 
    }
    public void setusername(String username) { 
    	this.username = username; 
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
	
	

}
