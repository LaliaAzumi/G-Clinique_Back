package com.erp.clinique.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.erp.clinique.model.Consultation;
import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.ConsultationRepository;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.service.UserService;

@RestController
@RequestMapping("/api/v1/consultations")
@CrossOrigin(origins = "http://localhost:8081")
public class ConsultationApiController {

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MedecinUserRepository medecinUserRepo;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long medecinId = getCurrentMedecinId();
        if (medecinId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Accès réservé aux médecins"));
        }

        Pageable sortedByDateDesc = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Consultation> consultationPage;

        if ((keyword != null && !keyword.isEmpty()) && date != null) {
            consultationPage = consultationRepository
                .findByRendezVousMedecinIdAndRendezVousPatientNomContainingIgnoreCaseAndDate(
                    medecinId, keyword, date, sortedByDateDesc);
        } else if (keyword != null && !keyword.isEmpty()) {
            consultationPage = consultationRepository
                .findByRendezVousMedecinIdAndRendezVousPatientNomContainingIgnoreCase(
                    medecinId, keyword, sortedByDateDesc);
        } else if (date != null) {
            consultationPage = consultationRepository
                .findByRendezVousMedecinIdAndDate(medecinId, date, sortedByDateDesc);
        } else {
            consultationPage = consultationRepository
                .findByRendezVousMedecinId(medecinId, sortedByDateDesc);
        }

        List<Map<String, Object>> consultations = consultationPage.getContent().stream()
            .map(c -> Map.of(
                "id", c.getId(),
                "date", c.getDate(),
                "diagnostique", c.getDiagnostique() != null ? c.getDiagnostique() : "",
                "maladie", c.getMaladie() != null ? c.getMaladie() : "",
                "patient", Map.of(
                    "id", c.getRendezVous().getPatient().getId(),
                    "nom", c.getRendezVous().getPatient().getNom(),
                    "prenom", c.getRendezVous().getPatient().getPrenom()
                ),
                "heure", c.getRendezVous().getHeure() != null ? c.getRendezVous().getHeure().toString() : "",
                "statut", c.getRendezVous().getStatut()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "consultations", consultations,
                "currentPage", page,
                "totalPages", consultationPage.getTotalPages(),
                "totalItems", consultationPage.getTotalElements()
            )
        ));
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayConsultations() {
        Long medecinId = getCurrentMedecinId();
        if (medecinId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Accès réservé aux médecins"));
        }

        LocalDate today = LocalDate.now();
        Pageable sortedByTime = PageRequest.of(0, 50, Sort.by("rendezVous.heure").ascending());
        
        Page<Consultation> consultationPage = consultationRepository
            .findByRendezVousMedecinIdAndDate(medecinId, today, sortedByTime);

        List<Map<String, Object>> consultations = consultationPage.getContent().stream()
            .map(c -> Map.of(
                "id", c.getId(),
                "date", c.getDate(),
                "diagnostique", c.getDiagnostique() != null ? c.getDiagnostique() : "",
                "maladie", c.getMaladie() != null ? c.getMaladie() : "",
                "patient", Map.of(
                    "id", c.getRendezVous().getPatient().getId(),
                    "nom", c.getRendezVous().getPatient().getNom(),
                    "prenom", c.getRendezVous().getPatient().getPrenom(),
                    "telephone", c.getRendezVous().getPatient().getTelephone() != null ? c.getRendezVous().getPatient().getTelephone() : ""
                ),
                "heure", c.getRendezVous().getHeure() != null ? c.getRendezVous().getHeure().toString() : "",
                "motif", c.getRendezVous().getMotif() != null ? c.getRendezVous().getMotif() : "",
                "statut", c.getRendezVous().getStatut()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "consultations", consultations,
                "date", today.toString(),
                "total", consultations.size()
            )
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Long medecinId = getCurrentMedecinId();
        if (medecinId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Accès réservé aux médecins"));
        }

        return consultationRepository.findById(id)
            .filter(c -> c.getRendezVous().getMedecin().getId().equals(medecinId))
            .map(c -> ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("consultation", c)
            )))
            .orElse(ResponseEntity.status(404).body(Map.of("success", false, "message", "Consultation non trouvée")));
    }

    private Long getCurrentMedecinId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Users user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            MedecinUser medUser = medecinUserRepo.findByUserId(user.getId());
            return medUser != null ? medUser.getMedecinId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
