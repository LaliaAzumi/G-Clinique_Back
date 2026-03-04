package com.erp.clinique.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicament")
public class Medicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String description;

   
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id= id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom= nom;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description= description;
	}
	
	public Medicament(String nom, String description) {
		this.nom = nom;
		this.description = description;
		
	}
	public Medicament() {}
    
}