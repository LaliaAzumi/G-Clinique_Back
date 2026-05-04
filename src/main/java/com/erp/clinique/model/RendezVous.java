package com.erp.clinique.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonManagedReference;
// import jakarta.persistence.OneToOne;

@Entity
@Table(name = "rendez_vous")
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le motif est obligatoire")
    @Column(nullable = false)
    private String motif;

    private LocalDate date;
    
    @Column(nullable = false)
    private LocalTime heure; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
//    @JsonIgnoreProperties("rendezVous")
    @JsonIgnoreProperties({"rendezVous", "hibernateLazyInitializer", "handler"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
//    @JsonIgnoreProperties("rendezVous")
    @JsonIgnoreProperties({"rendezVous", "hibernateLazyInitializer", "handler"})
    private Medecin medecin;

    @Column(nullable = false)
    private String statut = "EN_ATTENTE"; 
    
    @Column(nullable = false)
    private String statutPaiement = "EN_ATTENTE_PAIEMENT"; 

    @OneToOne(mappedBy = "rendezVous", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Consultation consultation;
    
    @Column(nullable = true)
    private  String googleEventId;

    @OneToOne(mappedBy = "rendezvous", fetch = FetchType.LAZY)
    @JsonManagedReference
    private Paiement paiement;
    
    @OneToMany(mappedBy = "rendezvous", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Prestation> prestations = new ArrayList<>();
    
    public List<Prestation> getPrestations() { return prestations; }
    public void setPrestations(List<Prestation> prestations) { this.prestations = prestations; }

   
    public RendezVous() {}
    public RendezVous(String motif, LocalDate date,LocalTime heure, Patient patient, Medecin medecin, String statut ) {
        this.motif = motif;
        this.date = date;
        this.patient = patient;
        this.medecin = medecin;
        this.statut = statut;
        this.heure = heure;
    }
    
    

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Medecin getMedecin() {
        return medecin;
    }

    public void setMedecin(Medecin medecin) {
        this.medecin = medecin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public String getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(String statutPaiement) {
        this.statutPaiement = statutPaiement;
    }
    
    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }
    public String getGoogleEventId() {
        return googleEventId;
    }

    public void setGoogleEventId(String googleEventId) {
        this.googleEventId = googleEventId;
    }
    public Paiement getPaiement() {
        return paiement;
    }

    public void setPaiement(Paiement paiement) {
        this.paiement = paiement;
    }
    
    // SECRETAIRE CHANGE STATUT APRES AVOIR PAYÉ
    public void validerPaiement() {
        // Utilise .equals() pour comparer les Strings en Java !
        if ("EN_ATTENTE_VALIDATION".equals(this.statut)) {
            this.statutPaiement = "PAYE";
            this.statut = "EN_ATTENTE";

            if (this.paiement != null) {
                this.paiement.setStatut("CONFIRME");
            }
        }
    }
}
