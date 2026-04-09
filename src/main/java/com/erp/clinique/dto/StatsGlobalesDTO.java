package com.erp.clinique.dto;

import java.util.List;

public class StatsGlobalesDTO {
    // Totaux bruts
    private double totalMedicaments;
    private double totalChambres;
    private double totalConsultations;
    private double revenuTotal;

    // Pourcentages (en format 0.00)
    private double pctMedicaments;
    private double pctChambres;
    private double pctConsultations;

    // Liste pour le graphique (si tu veux tout envoyer en un seul appel API)
    private List<RevenuMensuelDTO> detailMensuel;

    // Constructeur vide pour Jackson (JSON)
    public StatsGlobalesDTO() {}

    // Constructeur complet
    public StatsGlobalesDTO(double totalMedicaments, double totalChambres, double totalConsultations) {
        this.totalMedicaments = totalMedicaments;
        this.totalChambres = totalChambres;
        this.totalConsultations = totalConsultations;
        
        // Calcul du total général
        this.revenuTotal = totalMedicaments + totalChambres + totalConsultations;
        
        // Calcul automatique des pourcentages pour éviter la logique complexe sur le Front
        if (this.revenuTotal > 0) {
            this.pctMedicaments = Math.round((totalMedicaments / revenuTotal) * 10000.0) / 100.0;
            this.pctChambres = Math.round((totalChambres / revenuTotal) * 10000.0) / 100.0;
            this.pctConsultations = Math.round((totalConsultations / revenuTotal) * 10000.0) / 100.0;
        }
    }

    // --- Getters et Setters ---

    public double getTotalMedicaments() {
        return totalMedicaments;
    }

    public void setTotalMedicaments(double totalMedicaments) {
        this.totalMedicaments = totalMedicaments;
    }

    public double getTotalChambres() {
        return totalChambres;
    }

    public void setTotalChambres(double totalChambres) {
        this.totalChambres = totalChambres;
    }

    public double getTotalConsultations() {
        return totalConsultations;
    }

    public void setTotalConsultations(double totalConsultations) {
        this.totalConsultations = totalConsultations;
    }

    public double getRevenuTotal() {
        return revenuTotal;
    }

    public void setRevenuTotal(double revenuTotal) {
        this.revenuTotal = revenuTotal;
    }

    public double getPctMedicaments() {
        return pctMedicaments;
    }

    public double getPctChambres() {
        return pctChambres;
    }

    public double getPctConsultations() {
        return pctConsultations;
    }

    public List<RevenuMensuelDTO> getDetailMensuel() {
        return detailMensuel;
    }

    public void setDetailMensuel(List<RevenuMensuelDTO> detailMensuel) {
        this.detailMensuel = detailMensuel;
    }
}