package com.erp.clinique.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chambres")
public class Chambre {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numero;

    @Column(nullable = false, length = 10)
    private String nom; // Nom court ex: A01, B01

    @Column(nullable = false)
    @JsonProperty("etat")
    private boolean etat; // true = LIBRE, false = OCCUPÉE

    @Column(nullable = false, name = "prix_j")
    private Double prixJ;

    @Column(nullable = false, length = 10)
    private String etage; // "RDC", "1", "2", "3", "4"

    @Column(name = "is_soins_intensifs")
    @JsonProperty("soinsIntensifs")
    private boolean soinsIntensifs; // true = Soins Intensifs, false = Standard

    // --- CONSTRUCTEURS ---

    // Constructeur vide (Obligatoire pour Hibernate/JPA)
    public Chambre() {
    }

    // Constructeur avec paramètres (Utile pour créer des objets rapidement)
        // public Chambre(String numero, boolean etat, Double prixJ, Integer etage, boolean isSoinsIntensifs) {
    public Chambre(String numero, String nom, boolean etat, Double prixJ, String etage, boolean soinsIntensifs) {
        this.numero = numero;
        this.nom = nom;
        this.etat = etat;
        this.prixJ = prixJ;
        this.etage = etage;
        this.soinsIntensifs = soinsIntensifs;
    }

    // --- GETTERS ET SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @JsonProperty("etat")
    public boolean isEtat() { // Pour les booleans, la convention est 'is' au lieu de 'get'
        return etat;
    }

    public void setEtat(boolean etat) {
        this.etat = etat;
    }

    public Double getPrixJ() {
        return prixJ;
    }

    public void setPrixJ(Double prixJ) {
        this.prixJ = prixJ;
    }

    public String getEtage() {
        return etage;
    }

    public void setEtage(String etage) {
        this.etage = etage;
    }

    @JsonProperty("soinsIntensifs")
    public boolean isSoinsIntensifs() {
        return soinsIntensifs;
    }

    public void setSoinsIntensifs(boolean soinsIntensifs) {
        this.soinsIntensifs = soinsIntensifs;
    }
}