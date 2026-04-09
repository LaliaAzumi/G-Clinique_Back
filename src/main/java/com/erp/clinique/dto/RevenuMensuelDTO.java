package com.erp.clinique.dto;

public class RevenuMensuelDTO {
    private int mois;
    private double montant;

    public RevenuMensuelDTO(int mois, double montant) {
        this.mois = mois;
        this.montant = montant;
    }
    // Getters et Setters
    public int getMois() { return mois; }
    public double getMontant() { return montant; }
}