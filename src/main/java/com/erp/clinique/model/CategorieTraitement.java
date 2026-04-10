package com.erp.clinique.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories_traitement")
public class CategorieTraitement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom; // ex: Cardiologie, Dermatologie, Imagerie, Biologie

    private String description; // Description de la spécialité

    private String code; // Code unique ex: CARDIO, IMAGERIE, BIOL

    public CategorieTraitement() {}

    public CategorieTraitement(String nom, String description, String code) {
        this.nom = nom;
        this.description = description;
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
