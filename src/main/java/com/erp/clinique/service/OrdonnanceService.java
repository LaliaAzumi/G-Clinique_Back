// package com.erp.clinique.service;

// import java.io.File;
// import java.io.FileOutputStream;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;

// import org.springframework.stereotype.Service;

// import com.erp.clinique.model.Ordonnance;
// import com.erp.clinique.model.Prescription;
// import com.lowagie.text.Document;
// import com.lowagie.text.Element;
// import com.lowagie.text.Font;
// import com.lowagie.text.FontFactory;
// import com.lowagie.text.Paragraph;
// import com.lowagie.text.Phrase;
// import com.lowagie.text.pdf.PdfPCell;
// import com.lowagie.text.pdf.PdfPTable;
// import com.lowagie.text.pdf.PdfWriter;

// @Service
// public class OrdonnanceService {

// 	public void generateOrdonnancePdf(Ordonnance ordonnance, File file) throws Exception {
// 	    Document document = new Document();
// 	    PdfWriter.getInstance(document, new FileOutputStream(file));
// 	    document.open();

// 	    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
// 	    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
// 	    Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
// 		Font redFont = FontFactory.getFont(
// 			FontFactory.HELVETICA, 
// 			11, 
// 			java.awt.Color.RED
// 		);

// 	    Paragraph clinicName = new Paragraph("ERP CLINIQUE", titleFont);
// 	    clinicName.setAlignment(Element.ALIGN_CENTER);
// 	    document.add(clinicName);

// 	    String dateHeure = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
// 	    Paragraph datePara = new Paragraph("Date & Heure : " + dateHeure, normalFont);
// 	    datePara.setAlignment(Element.ALIGN_CENTER);
// 	    document.add(datePara);

// 	    document.add(new Paragraph(" ")); 

// 	    PdfPTable infoTable = new PdfPTable(2);
// 	    infoTable.setWidthPercentage(100);
// 	    infoTable.setWidths(new float[]{1, 1});

// 	    PdfPCell medecinCell = new PdfPCell();
// 	    medecinCell.setBorder(PdfPCell.NO_BORDER);
// 	    medecinCell.addElement(new Paragraph("Médecin :", headerFont));
// 	    medecinCell.addElement(new Paragraph("Nom : " + ordonnance.getConsultation().getRendezVous().getMedecin().getNom(), normalFont));
// 	    medecinCell.addElement(new Paragraph("Spécialité : " + ordonnance.getConsultation().getRendezVous().getMedecin().getSpecialite(), normalFont));
// 	    medecinCell.addElement(new Paragraph("Tel : " + ordonnance.getConsultation().getRendezVous().getMedecin().getTelephone(), normalFont));
// 	    medecinCell.addElement(new Paragraph("Adresse : " + ordonnance.getConsultation().getRendezVous().getMedecin().getAdresse(), normalFont));

// 	    PdfPCell patientCell = new PdfPCell();
// 	    patientCell.setBorder(PdfPCell.NO_BORDER);
// 	    patientCell.addElement(new Paragraph("Patient :", headerFont));
// 	    patientCell.addElement(new Paragraph("Nom : " + ordonnance.getConsultation().getRendezVous().getPatient().getNom() + " " + ordonnance.getConsultation().getRendezVous().getPatient().getPrenom(), normalFont));
// 	    patientCell.addElement(new Paragraph("Email : " + ordonnance.getConsultation().getRendezVous().getPatient().getEmail(), normalFont));
// 	    patientCell.addElement(new Paragraph("Tel : " + ordonnance.getConsultation().getRendezVous().getPatient().getTelephone(), normalFont));
// 	    patientCell.addElement(new Paragraph("Adresse : " + ordonnance.getConsultation().getRendezVous().getPatient().getAdresse(), normalFont));

// 	    infoTable.addCell(medecinCell);
// 	    infoTable.addCell(patientCell);
// 	    document.add(infoTable);

// 	    document.add(new Paragraph(" "));

// 	    document.add(new Paragraph("Ordonnance : " + ordonnance.getId(), normalFont));
// 	    document.add(new Paragraph("Motif : " + ordonnance.getConsultation().getRendezVous().getMotif(), normalFont));
// 	    document.add(new Paragraph("Maladie : " + ordonnance.getConsultation().getMaladie(), normalFont));

// 	    document.add(new Paragraph(" "));

// 	    PdfPTable table = new PdfPTable(5);
// 	    table.setWidthPercentage(100);
// 	    table.setWidths(new float[]{3, 2, 2, 1, 2});

// 	    String[] headers = {"Médicament", "Posologie", "Durée", "Qtt", "Prix total"};
// 	    for (String h : headers) {
// 	        PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
// 	        cell.setBorder(PdfPCell.NO_BORDER);
// 	        table.addCell(cell);
// 	    }

// 	    // double total = 0;
// 	    // for (Prescription p : ordonnance.getPrescriptions()) {
// 	    //     table.addCell(new PdfPCell(new Phrase(p.getMedicament().getNom(), normalFont)){{setBorder(PdfPCell.NO_BORDER);}});
// 	    //     table.addCell(new PdfPCell(new Phrase(p.getPosologie(), normalFont)){{setBorder(PdfPCell.NO_BORDER);}});
// 	    //     table.addCell(new PdfPCell(new Phrase(p.getDuree(), normalFont)){{setBorder(PdfPCell.NO_BORDER);}});
// 	    //     table.addCell(new PdfPCell(new Phrase(String.valueOf(p.getQuantite()), normalFont)){{setBorder(PdfPCell.NO_BORDER);}});
// 	    //     double prix = p.getQuantite() * p.getMedicament().getPu();
// 	    //     total += prix;
// 	    //     table.addCell(new PdfPCell(new Phrase(String.format("%.2f Ar", prix), normalFont)){{setBorder(PdfPCell.NO_BORDER);}});
// 	    // }
// 		double total = 0;

// for (Prescription p : ordonnance.getPrescriptions()) {

//     boolean horsStock = p.getMedicament().getQStock() <= 0;

//     Font fontToUse = horsStock ? redFont : normalFont;

//     table.addCell(new PdfPCell(new Phrase(p.getMedicament().getNom(), fontToUse)){{setBorder(PdfPCell.NO_BORDER);}});
//     table.addCell(new PdfPCell(new Phrase(p.getPosologie(), fontToUse)){{setBorder(PdfPCell.NO_BORDER);}});
//     table.addCell(new PdfPCell(new Phrase(p.getDuree(), fontToUse)){{setBorder(PdfPCell.NO_BORDER);}});
//     table.addCell(new PdfPCell(new Phrase(String.valueOf(p.getQuantite()), fontToUse)){{setBorder(PdfPCell.NO_BORDER);}});

//     double prix = 0;

//     if (!horsStock) {
//         prix = p.getQuantite() * p.getMedicament().getPu();
//         total += prix;
//     }

//     table.addCell(new PdfPCell(new Phrase(
//         horsStock ? "Non facturé" : String.format("%.2f Ar", prix),
//         fontToUse
//     )){{setBorder(PdfPCell.NO_BORDER);}});
// }

// 	    document.add(table);

// 	    document.add(new Paragraph(" "));
// 	    document.add(new Paragraph("Total à payer : " + String.format("%.2f Ar", total), headerFont));

// 	    document.add(new Paragraph(" "));

// 	    Paragraph fin = new Paragraph("La clinique vous remercie et vous souhaite un bon rétablissement !", normalFont);
// 	    fin.setAlignment(Element.ALIGN_CENTER);
// 	    document.add(fin);

// 	    document.close();
// 	}
// }

package com.erp.clinique.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.erp.clinique.model.Ordonnance;
import com.erp.clinique.model.Prescription;
import com.erp.clinique.model.Prestation;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class OrdonnanceService {

    private static final Long ACTE_A_EXCLURE = 1L;

    public void generateOrdonnancePdf(Ordonnance ordonnance, File file) throws Exception {

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font redFont = FontFactory.getFont(FontFactory.HELVETICA, 11, java.awt.Color.RED);

        // ---- HEADER ----
        Paragraph clinicName = new Paragraph("ERP CLINIQUE", titleFont);
        clinicName.setAlignment(Element.ALIGN_CENTER);
        document.add(clinicName);

        String dateHeure = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph datePara = new Paragraph("Date & Heure : " + dateHeure, normalFont);
        datePara.setAlignment(Element.ALIGN_CENTER);
        document.add(datePara);

        document.add(new Paragraph(" "));

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);

        PdfPCell medecinCell = new PdfPCell();
        medecinCell.setBorder(PdfPCell.NO_BORDER);
        medecinCell.addElement(new Paragraph("Médecin :", headerFont));
        medecinCell.addElement(new Paragraph("Nom : " + ordonnance.getConsultation().getRendezVous().getMedecin().getNom(), normalFont));
        medecinCell.addElement(new Paragraph("Spécialité : " + ordonnance.getConsultation().getRendezVous().getMedecin().getSpecialite(), normalFont));

        PdfPCell patientCell = new PdfPCell();
        patientCell.setBorder(PdfPCell.NO_BORDER);
        patientCell.addElement(new Paragraph("Patient :", headerFont));
        patientCell.addElement(new Paragraph("Nom : " +
                ordonnance.getConsultation().getRendezVous().getPatient().getNom() + " " +
                ordonnance.getConsultation().getRendezVous().getPatient().getPrenom(), normalFont));

        infoTable.addCell(medecinCell);
        infoTable.addCell(patientCell);
        document.add(infoTable);

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Ordonnance : " + ordonnance.getId(), normalFont));
        document.add(new Paragraph("Motif : " + ordonnance.getConsultation().getRendezVous().getMotif(), normalFont));
        document.add(new Paragraph("Maladie : " + ordonnance.getConsultation().getMaladie(), normalFont));

        document.add(new Paragraph(" "));

        // ---- MEDICAMENTS ----
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        String[] headers = {"Médicament", "Posologie", "Durée", "Qtt", "Prix"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBorder(PdfPCell.NO_BORDER);
            table.addCell(cell);
        }

        double totalMed = 0;

        for (Prescription p : ordonnance.getPrescriptions()) {

            boolean horsStock = p.getMedicament().getQStock() <= 0;
            Font fontToUse = horsStock ? redFont : normalFont;

            table.addCell(new PdfPCell(new Phrase(p.getMedicament().getNom(), fontToUse)) {{
                setBorder(PdfPCell.NO_BORDER);
            }});

            table.addCell(new PdfPCell(new Phrase(p.getPosologie(), fontToUse)) {{
                setBorder(PdfPCell.NO_BORDER);
            }});

            table.addCell(new PdfPCell(new Phrase(p.getDuree(), fontToUse)) {{
                setBorder(PdfPCell.NO_BORDER);
            }});

            table.addCell(new PdfPCell(new Phrase(String.valueOf(p.getQuantite()), fontToUse)) {{
                setBorder(PdfPCell.NO_BORDER);
            }});

            double prix = 0;

            if (!horsStock) {
                prix = p.getQuantite() * p.getMedicament().getPu();
                totalMed += prix;
            }

            table.addCell(new PdfPCell(new Phrase(
                    horsStock ? "Non facturé" : String.format("%.2f Ar", prix),
                    fontToUse
            )) {{
                setBorder(PdfPCell.NO_BORDER);
            }});
        }

        document.add(table);

        // ---- ACTES MEDICAUX ----
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Actes médicaux :", headerFont));

        PdfPTable acteTable = new PdfPTable(2);
        acteTable.setWidthPercentage(100);

        acteTable.addCell(new PdfPCell(new Phrase("Acte", headerFont)) {{
            setBorder(PdfPCell.NO_BORDER);
        }});
        acteTable.addCell(new PdfPCell(new Phrase("Prix", headerFont)) {{
            setBorder(PdfPCell.NO_BORDER);
        }});

        double totalActes = 0;

        for (Prestation p : ordonnance.getConsultation().getRendezVous().getPrestations()) {

            if (p.getActe() == null) continue;

            if (p.getActe().getId().equals(ACTE_A_EXCLURE)) continue;

            String nom = p.getActe().getLibelle() != null
                    ? p.getActe().getLibelle()
                    : p.getActe().getLibelle();

            double prix = p.getPrixApplique() != null
                    ? p.getPrixApplique()
                    : p.getActe().getPrix();

            acteTable.addCell(new PdfPCell(new Phrase(nom, normalFont)) {{
                setBorder(PdfPCell.NO_BORDER);
            }});

            acteTable.addCell(new PdfPCell(new Phrase(prix + " Ar", normalFont)) {{
                setBorder(PdfPCell.NO_BORDER);
            }});

            totalActes += prix;
        }

        document.add(acteTable);

        // ---- TOTAL ----
        double totalGlobal = totalMed + totalActes;

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total médicaments : " + String.format("%.2f Ar", totalMed), normalFont));
        document.add(new Paragraph("Total actes : " + String.format("%.2f Ar", totalActes), normalFont));
        document.add(new Paragraph("Total à payer : " + String.format("%.2f Ar", totalGlobal), headerFont));

        document.add(new Paragraph(" "));

        Paragraph fin = new Paragraph("Merci et bon rétablissement !", normalFont);
        fin.setAlignment(Element.ALIGN_CENTER);
        document.add(fin);

        document.close();
    }
}