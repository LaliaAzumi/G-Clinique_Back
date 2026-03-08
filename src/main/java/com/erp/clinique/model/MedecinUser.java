package com.erp.clinique.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class MedecinUser {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long medecinId;

    @Column(unique = true, nullable = false)
    private Long userId;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Long medecinId) {
        this.medecinId = medecinId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public MedecinUser() {}

    public MedecinUser(Long medecinId, Long userId) {
        this.medecinId = medecinId;
        this.userId = userId;
    }
}
