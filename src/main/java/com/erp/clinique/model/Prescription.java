package com.erp.clinique.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescription")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String posologie;
    private String duree;
    private Integer quantite;

    @ManyToOne
    @JoinColumn(name = "ordonnance_id", nullable = false)
    private Ordonnance ordonnance;

    @ManyToOne
    @JoinColumn(name = "medicament_id", nullable = false)
    private Medicament medicament;

    public Prescription() {}
    public Prescription(String posologie, String duree, Integer quantite, Ordonnance ordonnance, Medicament medicament) {
    	this.posologie= posologie;
    	this.duree = duree;
    	this.quantite = quantite;
    	this.ordonnance = ordonnance;
    	this.medicament = medicament;
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
	public Medicament getMedicament() {
		return medicament;
	}
	public void setMedicament(Medicament medicament) {
		this.medicament = medicament;
	}
	
	public Ordonnance getOrdonnance() {
		return ordonnance;
	}
	public void setOrdonnance(Ordonnance ordonnance) {
		this.ordonnance = ordonnance;
	}

    
}