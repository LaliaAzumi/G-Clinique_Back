package com.erp.clinique.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordonnance")
public class Ordonnance {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @OneToMany(mappedBy = "ordonnance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();
    
    private String pdfPath;
    private Boolean paye = false;
    
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id= id;
	}
	public Consultation getConsultation() {
		return consultation;
	}
	public void setConsultation(Consultation consultation) {
		this.consultation= consultation;
	}
	public List<Prescription> getPrescriptions() {
		return prescriptions;
	}
	public void setPrescriptions(List<Prescription> prescriptions) {
	    this.prescriptions = prescriptions;
	}
	
	public Ordonnance(Consultation consultation, List<Prescription> prescriptions) {
		this.consultation = consultation;
		this.prescriptions = prescriptions;
	}
	public Ordonnance () {}
	public Boolean getPaye() {
		return paye;
	}
	public void setPaye(Boolean paye) {
		this.paye = paye;
	}
	public String getPdfPath() {
		return pdfPath;
	}
	public void setPdfPath(String pdfPath) {
		this.pdfPath = pdfPath;
	}

	public String getClientName() {
	    if (consultation != null && consultation.getRendezVous() != null 
	        && consultation.getRendezVous().getPatient() != null) {
	        return consultation.getRendezVous().getPatient().getNom() +" "+consultation.getRendezVous().getPatient().getPrenom(); // ou getFullName()
	    }
	    return "";
	}
}
