package com.erp.clinique.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
public class Patient {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotNull(message = "Nom obligatoire")
	@NotBlank(message = "Nom non vide")
	private String nom;
	
	@NotNull(message = "Prénom obligatoire")
	@NotBlank(message = "Prénom non vide")
	private String prenom;
	
	private LocalDate dateNaissance;
	
	@NotNull(message = "Téléphone obligatoire")
	@Pattern(regexp = "\\d{10}", message = "Numéro doit avoir exactement 10 chiffres")
	private String telephone;
	
	@NotNull(message = "Adresse obligatoire")
	@NotBlank(message = "Adresse non vide")
	private String adresse;
	
	@Column(nullable = false, unique = true)
	@NotNull(message = "email obligatoire")
	@NotBlank(message = "email non vide")
	private String email;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id= id;
	}
	public void setNom(Long id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	public LocalDate getDateNaissance() {
		return dateNaissance;
	}
	public void setDateNaissance(LocalDate dateNaissance) {
		this.dateNaissance = dateNaissance;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getAdresse() {
		return adresse;
	}
	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}
	
	public Patient() {}

    public Patient(String nom, String prenom, LocalDate dateNaissance, String telephone, String email, String adresse) {
        this.nom = nom;
        this.prenom = prenom; 
        this.dateNaissance = dateNaissance; 
        this.telephone = telephone;
        this.adresse = adresse;
        this.email = email;
    }
	
    @Override
    public String toString() {
        return "Patient . " + nom + " " + prenom + " "+ dateNaissance + "" +telephone +" "+ adresse;
    }
	
	

}
