package com.erp.clinique.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "traitements")
public class Traitement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom; // ex: Aspirine, Scanner Thoracique, NFS, etc.

    private String description; // Description détaillée

    private String type; // MEDICAMENT ou ACTE_MEDICAL

    private Double prix; // Prix unitaire

    @ManyToOne
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieTraitement categorie;

    // Pour les médicaments
    private String forme; // Comprimé, Sirop, Injection, etc.
    private String dosage; // 500mg, 1g, etc.

    public Traitement() {}

    public Traitement(String nom, String description, String type, Double prix, 
                      CategorieTraitement categorie, String forme, String dosage) {
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.prix = prix;
        this.categorie = categorie;
        this.forme = forme;
        this.dosage = dosage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public CategorieTraitement getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieTraitement categorie) {
        this.categorie = categorie;
    }

    public String getForme() {
        return forme;
    }

    public void setForme(String forme) {
        this.forme = forme;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
