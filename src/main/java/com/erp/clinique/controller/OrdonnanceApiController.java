package com.erp.clinique.controller;

import com.erp.clinique.model.*;
import com.erp.clinique.repository.*;
import com.erp.clinique.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ordonnances")
@CrossOrigin(origins = "http://localhost:8081")
public class OrdonnanceApiController {

    @Autowired
    private OrdonnanceRepository ordonnanceRepository;

    @Autowired
    private OrdonnanceTraitementRepository ordonnanceTraitementRepository;

    @Autowired
    private TraitementRepository traitementRepository;

    @Autowired
    private CategorieTraitementRepository categorieRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private MedecinUserRepository medecinUserRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private MedecinRepository medecinRepository;

    // Récupérer les traitements par spécialité du médecin connecté
    @GetMapping("/traitements")
    public ResponseEntity<?> getTraitementsBySpecialite(
            @RequestParam(required = false) Long categorieId,
            @RequestParam(required = false) String type) {

        Long medecinId = getCurrentMedecinId();
        if (medecinId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Accès réservé aux médecins"));
        }

        // Récupérer la spécialité du médecin
        Optional<Medecin> medecinOpt = medecinRepository.findById(medecinId);
        if (medecinOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Médecin non trouvé"));
        }

        String specialite = medecinOpt.get().getSpecialite();

        // Trouver la catégorie correspondant à la spécialité
        List<CategorieTraitement> categories;
        if (categorieId != null) {
            categories = categorieRepository.findById(categorieId)
                .map(List::of)
                .orElse(List.of());
        } else {
            // Chercher par nom de spécialité ou retourner toutes les catégories
            categories = categorieRepository.findAll();
        }

        // Récupérer les traitements
        List<Traitement> traitements;
        if (type != null && !type.isEmpty()) {
            traitements = traitementRepository.findByType(type);
        } else {
            traitements = traitementRepository.findAll();
        }

        // Filtrer par catégorie si spécifiée
        if (categorieId != null) {
            traitements = traitements.stream()
                .filter(t -> t.getCategorie().getId().equals(categorieId))
                .collect(Collectors.toList());
        }

        // Grouper par catégorie
        Map<String, List<Map<String, Object>>> traitementsParCategorie = traitements.stream()
            .collect(Collectors.groupingBy(
                t -> t.getCategorie().getNom(),
                Collectors.mapping(t -> Map.of(
                    "id", t.getId(),
                    "nom", t.getNom(),
                    "description", t.getDescription() != null ? t.getDescription() : "",
                    "type", t.getType(),
                    "prix", t.getPrix(),
                    "forme", t.getForme() != null ? t.getForme() : "",
                    "dosage", t.getDosage() != null ? t.getDosage() : "",
                    "categorie", Map.of(
                        "id", t.getCategorie().getId(),
                        "nom", t.getCategorie().getNom(),
                        "code", t.getCategorie().getCode()
                    )
                ), Collectors.toList())
            ));

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "specialiteMedecin", specialite,
                "traitementsParCategorie", traitementsParCategorie,
                "categories", categories.stream().map(c -> Map.of(
                    "id", c.getId(),
                    "nom", c.getNom(),
                    "code", c.getCode(),
                    "description", c.getDescription() != null ? c.getDescription() : ""
                )).collect(Collectors.toList())
            )
        ));
    }

    // Créer une ordonnance avec traitements
    @PostMapping
    public ResponseEntity<?> createOrdonnance(@RequestBody Map<String, Object> request) {
        Long medecinId = getCurrentMedecinId();
        if (medecinId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Accès réservé aux médecins"));
        }

        try {
            Long consultationId = Long.valueOf(request.get("consultationId").toString());
            List<Map<String, Object>> traitementsData = (List<Map<String, Object>>) request.get("traitements");

            // Vérifier que la consultation existe et appartient au médecin
            Optional<Consultation> consultationOpt = consultationRepository.findById(consultationId);
            if (consultationOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Consultation non trouvée"));
            }

            Consultation consultation = consultationOpt.get();
            if (!consultation.getRendezVous().getMedecin().getId().equals(medecinId)) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Cette consultation ne vous appartient pas"));
            }

            // Créer l'ordonnance
            Ordonnance ordonnance = new Ordonnance();
            ordonnance.setConsultation(consultation);
            ordonnance.setPaye(false);
            ordonnance = ordonnanceRepository.save(ordonnance);

            // Ajouter les traitements
            double total = 0;
            for (Map<String, Object> tData : traitementsData) {
                Long traitementId = Long.valueOf(tData.get("traitementId").toString());
                Traitement traitement = traitementRepository.findById(traitementId)
                    .orElseThrow(() -> new RuntimeException("Traitement non trouvé: " + traitementId));

                OrdonnanceTraitement ot = new OrdonnanceTraitement();
                ot.setOrdonnance(ordonnance);
                ot.setTraitement(traitement);
                ot.setPosologie((String) tData.get("posologie"));
                ot.setDuree((String) tData.get("duree"));
                ot.setQuantite(Integer.valueOf(tData.get("quantite").toString()));
                ot.setInstructions((String) tData.get("instructions"));
                ordonnanceTraitementRepository.save(ot);

                total += traitement.getPrix() * ot.getQuantite();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ordonnance créée avec succès",
                "data", Map.of(
                    "ordonnanceId", ordonnance.getId(),
                    "total", total
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Récupérer l'ordonnance d'une consultation
    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<?> getOrdonnanceByConsultation(@PathVariable Long consultationId) {
        Long medecinId = getCurrentMedecinId();
        if (medecinId == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Accès réservé aux médecins"));
        }

        Optional<Consultation> consultationOpt = consultationRepository.findById(consultationId);
        if (consultationOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Consultation non trouvée"));
        }

        Consultation consultation = consultationOpt.get();
        if (!consultation.getRendezVous().getMedecin().getId().equals(medecinId)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Accès non autorisé"));
        }

        Ordonnance ordonnance = consultation.getOrdonnance();
        if (ordonnance == null) {
            return ResponseEntity.ok(Map.of("success", true, "data", null));
        }

        List<OrdonnanceTraitement> traitements = ordonnanceTraitementRepository.findByOrdonnanceId(ordonnance.getId());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "ordonnance", Map.of(
                    "id", ordonnance.getId(),
                    "paye", ordonnance.getPaye(),
                    "pdfPath", ordonnance.getPdfPath() != null ? ordonnance.getPdfPath() : "",
                    "traitements", traitements.stream().map(t -> Map.of(
                        "id", t.getId(),
                        "traitement", Map.of(
                            "nom", t.getTraitement().getNom(),
                            "type", t.getTraitement().getType(),
                            "prix", t.getTraitement().getPrix()
                        ),
                        "posologie", t.getPosologie(),
                        "duree", t.getDuree(),
                        "quantite", t.getQuantite(),
                        "instructions", t.getInstructions() != null ? t.getInstructions() : ""
                    )).collect(Collectors.toList())
                )
            )
        ));
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
