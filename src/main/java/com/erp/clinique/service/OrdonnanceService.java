package com.erp.clinique.service;

import java.io.File;
import java.io.FileOutputStream;

import org.springframework.stereotype.Service;

import com.erp.clinique.model.Ordonnance;
import com.erp.clinique.model.Prescription;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class OrdonnanceService {

    public File generateOrdonnancePdf(Ordonnance ordonnance) throws Exception {
        String filename = "ordonnance_" + ordonnance.getId() + ".pdf";
        File file = new File(filename);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        document.add(new Paragraph("Ordonnance - Consultation #" + ordonnance.getConsultation().getId()));
        document.add(new Paragraph("Patient : " + ordonnance.getConsultation().getRendezVous().getPatient().getNom()));
        document.add(new Paragraph(" ")); // saut de ligne

        // Table pour les prescriptions
        PdfPTable table = new PdfPTable(5); // 5 colonnes
        table.addCell("Médicament");
        table.addCell("Posologie");
        table.addCell("Durée");
        table.addCell("Quantité");
        table.addCell("Prix total");

        double total = 0;
        for (Prescription p : ordonnance.getPrescriptions()) {
            table.addCell(p.getMedicament().getNom());
            table.addCell(p.getPosologie());
            table.addCell(p.getDuree());
            table.addCell(String.valueOf(p.getQuantite()));
            double prix = p.getQuantite() * p.getMedicament().getPu();
            table.addCell(String.format("%.2f Ar", prix));
            total += prix;
        }

        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total : " + String.format("%.2f Ar", total)));

        document.close();

        return file;
    }
}