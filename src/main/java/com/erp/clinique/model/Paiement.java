package com.erp.clinique.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;


@Entity
public class Paiement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nomExpediteur;
    @Column(unique = true)
    private String codeTransaction;
    private Double montantEnvoye;
    private String statut; // "EN_COURS", "VALIDE", "REFUSE"
    
    private String motif;


    @OneToOne
    @JoinColumn(name = "rendezvous_id")
    @JsonBackReference
    private RendezVous rendezvous;

    public Paiement() {}
    
 // Constructeur complet
    public Paiement(String nomExpediteur, String codeTransaction, Double montantEnvoye, RendezVous rendezvous, String motif) {
        this.nomExpediteur = nomExpediteur;
        this.codeTransaction = codeTransaction;
        this.montantEnvoye = montantEnvoye;
        this.statut = "EN_COURS";
        this.rendezvous = rendezvous;
        this.motif = motif;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomExpediteur() { return nomExpediteur; }
    public void setNomExpediteur(String nomExpediteur) { this.nomExpediteur = nomExpediteur; }
    public String getCodeTransaction() { return codeTransaction; }
    public void setCodeTransaction(String codeTransaction) { this.codeTransaction = codeTransaction; }
    public Double getMontantEnvoye() { return montantEnvoye; }
    public void setMontantEnvoye(Double montantEnvoye) { this.montantEnvoye = montantEnvoye; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public RendezVous getRendezvous() { return rendezvous; }
    public void setRendezvous(RendezVous rendezvous) { this.rendezvous = rendezvous; }

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}
}