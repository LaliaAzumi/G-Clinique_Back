package com.erp.clinique.dto;

import java.util.List;
import java.util.Map;

public class DashboardDTO {

    // KPI
    private long totalMedecins;
    private long totalPatients;

    private long totalRdv;
    private long rdvEnAttente;
    private long rdvTermine;

    private Double totalPaiement;
    private Double totalMedicaments;
    private Double totalPrestations;

    // Graphs
    private List<Map<String, Object>> rdvParJour;
    private List<Map<String, Object>> paiementParJour;

    // Getters & Setters
    public long getTotalMedecins() { return totalMedecins; }
    public void setTotalMedecins(long totalMedecins) { this.totalMedecins = totalMedecins; }

    public long getTotalPatients() { return totalPatients; }
    public void setTotalPatients(long totalPatients) { this.totalPatients = totalPatients; }

    public long getTotalRdv() { return totalRdv; }
    public void setTotalRdv(long totalRdv) { this.totalRdv = totalRdv; }

    public long getRdvEnAttente() { return rdvEnAttente; }
    public void setRdvEnAttente(long rdvEnAttente) { this.rdvEnAttente = rdvEnAttente; }

    public long getRdvTermine() { return rdvTermine; }
    public void setRdvTermine(long rdvTermine) { this.rdvTermine = rdvTermine; }

    public Double getTotalPaiement() { return totalPaiement; }
    public void setTotalPaiement(Double totalPaiement) { this.totalPaiement = totalPaiement; }

    public Double getTotalMedicaments() { return totalMedicaments; }
    public void setTotalMedicaments(Double totalMedicaments) { this.totalMedicaments = totalMedicaments; }

    public Double getTotalPrestations() { return totalPrestations; }
    public void setTotalPrestations(Double totalPrestations) { this.totalPrestations = totalPrestations; }

    public List<Map<String, Object>> getRdvParJour() { return rdvParJour; }
    public void setRdvParJour(List<Map<String, Object>> rdvParJour) { this.rdvParJour = rdvParJour; }

    public List<Map<String, Object>> getPaiementParJour() { return paiementParJour; }
    public void setPaiementParJour(List<Map<String, Object>> paiementParJour) { this.paiementParJour = paiementParJour; }
}