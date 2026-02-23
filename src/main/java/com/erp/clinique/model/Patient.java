package com.erp.clinique.model;

import java.time.LocalDate;

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
	@Pattern(regexp = "\\d{10}", message = "Numéro 10 chiffres")
	private Long telephone;
	
	@NotNull(message = "Adresse obligatoire")
	@NotBlank(message = "Adresse non vide")
	private String adresse;
	
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
	public Long getTelephone() {
		return telephone;
	}
	public void setTelephone(Long telephone) {
		this.telephone = telephone;
	}
	public String getAdresse() {
		return adresse;
	}
	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}
	
	
	

}
