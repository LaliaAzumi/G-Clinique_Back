package com.erp.clinique.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "prestations")
public class Prestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rendezvous_id")
    @JsonIgnore
    private RendezVous rendezvous;

    @ManyToOne
    @JoinColumn(name = "acte_id")
    private ActeMedical acte;

    private Double prixApplique; // On enregistre le prix payé au moment du RDV

    @Column(columnDefinition = "TEXT")
    private String resultat; // Le compte-rendu du médecin

    // Constructeur vide (Obligatoire pour JPA/Hibernate)
    public Prestation() {}

    // Constructeur pratique
    public Prestation(RendezVous rendezvous, ActeMedical acte, Double prixApplique) {
        this.rendezvous = rendezvous;
        this.acte = acte;
        this.prixApplique = prixApplique;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RendezVous getRendezvous() { return rendezvous; }
    public void setRendezvous(RendezVous rendezvous) { this.rendezvous = rendezvous; }

    public ActeMedical getActe() { return acte; }
    public void setActe(ActeMedical acte) { this.acte = acte; }

    public Double getPrixApplique() { return prixApplique; }
    public void setPrixApplique(Double prixApplique) { this.prixApplique = prixApplique; }

    public String getResultat() { return resultat; }
    public void setResultat(String resultat) { this.resultat = resultat; }
}