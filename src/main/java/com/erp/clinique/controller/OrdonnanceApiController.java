package com.erp.clinique.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.erp.clinique.model.Ordonnance;
import com.erp.clinique.model.Prescription;
import com.erp.clinique.repository.OrdonnanceRepository;
import com.erp.clinique.service.OrdonnanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1/ordonnances")
public class OrdonnanceApiController {

    private final OrdonnanceRepository ordonnanceRepository;
    private final OrdonnanceService ordonnanceService;

    public OrdonnanceApiController(OrdonnanceRepository ordonnanceRepository, OrdonnanceService ordonnanceService) {
        this.ordonnanceRepository = ordonnanceRepository;
        this.ordonnanceService = ordonnanceService;
    }

    // Rechercher par nom de patient
    // Dans OrdonnanceApiController.java
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchByPatient(@RequestParam String name) {
        // On utilise findByPatientName que tu as déjà défini avec ton @Query
        return ResponseEntity.ok(
            ordonnanceRepository.findByPatientName(name)
                .stream()
                .map(this::toDto)
                .toList()
        );
    }

    // Marquer comme payé
    @PutMapping("/{id}/pay")
    public ResponseEntity<Map<String, Object>> markAsPaid(@PathVariable Long id) {
        return ordonnanceRepository.findById(id)
            .map(ord -> {
                ord.setPaye(true);
                return ResponseEntity.ok(toDto(ordonnanceRepository.save(ord)));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> getPdf(@PathVariable Long id) throws Exception {
        Ordonnance ordonnance = ordonnanceRepository.findById(id).orElse(null);
        if (ordonnance == null) {
            return ResponseEntity.notFound().build();
        }

        File pdfFile = resolvePdfFile(ordonnance);
        if (!pdfFile.exists()) {
            File dir = pdfFile.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            ordonnanceService.generateOrdonnancePdf(ordonnance, pdfFile);
            ordonnance.setPdfPath(pdfFile.getAbsolutePath());
            ordonnanceRepository.save(ordonnance);
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"ordonnance_" + id + ".pdf\"")
            .body(new FileSystemResource(pdfFile));
    }

    private Map<String, Object> toDto(Ordonnance ordonnance) {
        return Map.of(
            "id", ordonnance.getId(),
            "patientName", ordonnance.getClientName(),
            "date", ordonnance.getConsultation() != null ? ordonnance.getConsultation().getDate() : "",
            "paye", Boolean.TRUE.equals(ordonnance.getPaye()),
            "pdfUrl", "/api/v1/ordonnances/" + ordonnance.getId() + "/pdf",
            "prescriptions", ordonnance.getPrescriptions().stream().map(this::prescriptionToDto).toList()
        );
    }

    private Map<String, Object> prescriptionToDto(Prescription prescription) {
        return Map.of(
            "medicament", prescription.getMedicament() != null ? prescription.getMedicament().getNom() : "",
            "posologie", prescription.getPosologie() != null ? prescription.getPosologie() : "",
            "duree", prescription.getDuree() != null ? prescription.getDuree() : "",
            "quantite", prescription.getQuantite() != null ? prescription.getQuantite() : 0
        );
    }

    private File resolvePdfFile(Ordonnance ordonnance) {
        if (ordonnance.getPdfPath() != null && !ordonnance.getPdfPath().isBlank()) {
            File savedFile = new File(ordonnance.getPdfPath());
            if (savedFile.exists()) {
                return savedFile;
            }
        }

        File dockerFile = new File("/app/pdf_ordonnances/ordonnance_" + ordonnance.getId() + ".pdf");
        if (dockerFile.exists() || dockerFile.getParentFile().canWrite()) {
            return dockerFile;
        }

        return new File(
            System.getProperty("user.dir"),
            "pdf_ordonnances/ordonnance_" + ordonnance.getId() + ".pdf"
        );
    }
}
