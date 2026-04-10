package com.erp.clinique.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordonnance_traitements")
public class OrdonnanceTraitement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ordonnance_id", nullable = false)
    private Ordonnance ordonnance;

    @ManyToOne
    @JoinColumn(name = "traitement_id", nullable = false)
    private Traitement traitement;

    private String posologie; // ex: 1 comprimé matin et soir
    private String duree; // ex: 7 jours
    private Integer quantite; // ex: 14
    private String instructions; // Instructions spécifiques

    public OrdonnanceTraitement() {}

    public OrdonnanceTraitement(Ordonnance ordonnance, Traitement traitement, 
                                String posologie, String duree, Integer quantite, String instructions) {
        this.ordonnance = ordonnance;
        this.traitement = traitement;
        this.posologie = posologie;
        this.duree = duree;
        this.quantite = quantite;
        this.instructions = instructions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ordonnance getOrdonnance() {
        return ordonnance;
    }

    public void setOrdonnance(Ordonnance ordonnance) {
        this.ordonnance = ordonnance;
    }

    public Traitement getTraitement() {
        return traitement;
    }

    public void setTraitement(Traitement traitement) {
        this.traitement = traitement;
    }

    public String getPosologie() {
        return posologie;
    }

    public void setPosologie(String posologie) {
        this.posologie = posologie;
    }

    public String getDuree() {
        return duree;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
