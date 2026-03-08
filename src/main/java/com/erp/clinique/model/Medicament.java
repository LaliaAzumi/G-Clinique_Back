package com.erp.clinique.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicament")
public class Medicament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String description;
    private  double pu;
    private Integer qStock;

   
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id= id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom= nom;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description= description;
	}
	
	public Medicament(String nom, String description, double pu, Integer qStock) {
		this.nom = nom;
		this.description = description;
		this.pu = pu;
		this.qStock = qStock;
		
	}
	public Medicament() {}
	public double getPu() {
		return pu;
	}
	public void setPu(double pu) {
		this.pu = pu;
	}
	public Integer getqStock() {
		return qStock;
	}
	public void setqStock(Integer qStock) {
		this.qStock = qStock;
	}
}