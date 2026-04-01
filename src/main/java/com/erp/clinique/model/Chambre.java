package com.erp.clinique.model;

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

    @Column(nullable = false)
    private boolean etat; // true = LIBRE, false = OCCUPÉE

    @Column(nullable = false, name = "prix_j")
    private Double prixJ;

    private Integer etage;

    @Column(name = "is_soins_intensifs")
    private boolean isSoinsIntensifs; // true = Soins Intensifs, false = Standard

    // --- CONSTRUCTEURS ---

    // Constructeur vide (Obligatoire pour Hibernate/JPA)
    public Chambre() {
    }

    // Constructeur avec paramètres (Utile pour créer des objets rapidement)
    public Chambre(String numero, boolean etat, Double prixJ, Integer etage, boolean isSoinsIntensifs) {
        this.numero = numero;
        this.etat = etat;
        this.prixJ = prixJ;
        this.etage = etage;
        this.isSoinsIntensifs = isSoinsIntensifs;
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

    public Integer getEtage() {
        return etage;
    }

    public void setEtage(Integer etage) {
        this.etage = etage;
    }

    public boolean isSoinsIntensifs() {
        return isSoinsIntensifs;
    }

    public void setSoinsIntensifs(boolean soinsIntensifs) {
        this.isSoinsIntensifs = soinsIntensifs;
    }
}