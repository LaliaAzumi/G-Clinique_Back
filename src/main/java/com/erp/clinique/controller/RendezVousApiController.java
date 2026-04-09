package com.erp.clinique.controller;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.model.Patient;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.repository.PrestationRepository;
import com.erp.clinique.service.RendezVousService;

@RestController
@RequestMapping("/api/v1/rendez-vous")
public class RendezVousApiController {

    @Autowired
    private RendezVousService rendezVousService;
    @Autowired
    private PrestationRepository prestationRepository;

    //create rdv par le patient
    @PostMapping("/save-public")
    public ResponseEntity<?> savePublic(@RequestBody Map<String, Object> data) {
        try {	
            // 1. Extraction manuelle des données du Map
            // On crée l'objet Patient à la volée
            Patient p = new Patient();
            p.setNom((String) data.get("nom"));
            p.setPrenom((String) data.get("prenom"));
            p.setEmail((String) data.get("email"));
            String dateStr = (String) data.get("datenaissance");
            LocalDate localDate = LocalDate.parse(dateStr); 
            p.setDateNaissance(localDate);
            
            //p.setDateNaissance((Date) data.get("datenaissance"));
            p.setTelephone((String) data.get("telephone"));
            p.setAdresse((String) data.get("adresse"));

            // 2. Récupération des autres paramètres
            Long medecinId = Long.valueOf(data.get("medecinId").toString());
            LocalDate date = LocalDate.parse(data.get("date").toString());
            LocalTime heure = LocalTime.parse(data.get("heure").toString());
            
            // Récupération de la liste des IDs d'actes
            List<Integer> acteIdsInt = (List<Integer>) data.get("acteIds");
            List<Long> acteIds = acteIdsInt.stream()
                                          .map(Long::valueOf)
                                          .toList();

            if (acteIds.isEmpty()) {
                acteIds.add(1L); 
            }
            // Infos Paiement
            String nomExpediteur = (String) data.get("nomExpediteur");
            String codeTransaction = (String) data.get("codeTransaction");
            Double montantEnvoye = Double.valueOf(data.get("montantEnvoye").toString());

            // 3. Appel de ton service
            RendezVous rdv = rendezVousService.enregistrerRendezVousComplet(
                p, medecinId, date, heure, acteIds, 
                nomExpediteur, codeTransaction, montantEnvoye
            );

            return ResponseEntity.ok(Map.of("status", "success", "id", rdv.getId()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    //lister les rdv
    @GetMapping
    public ResponseEntity<List<RendezVous>> list(
        @RequestParam(required = false) Long patientId,
        @RequestParam(required = false) Long medecinId,
        @RequestParam(required = false) String date
    ) {
        // Ici, tu appelles ton service. 
        // Si tu n'as pas encore de méthode de filtrage, on liste tout :
        List<RendezVous> rendezVous = rendezVousService.findAll(); 
        return ResponseEntity.ok(rendezVous);
    }
    
    
    //prendre un rdv by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return rendezVousService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    
    //update status paiement par secretaire
    @PatchMapping("/{id}/valider-paiement")
    public ResponseEntity<?> validerPaiement(@PathVariable Long id) {
        try {
            // 1. Chercher le rendez-vous
            return rendezVousService.findById(id).map(rdv -> {
                // 2. Appliquer la logique métier que tu as créée dans le modèle
                rdv.validerPaiement(); 
                
                // 3. Sauvegarder les changements en base de données
                rendezVousService.save(rdv);
                
                // 4. Répondre avec le nouveau statut pour confirmation
                return ResponseEntity.ok(Map.of(
                    "message", "Paiement validé avec succès",
                    "id", rdv.getId(),
                    "nouveauStatutPaiement", rdv.getStatutPaiement(),
                    "nouveauStatutRDV", rdv.getStatut()
                ));
            }).orElse(ResponseEntity.notFound().build());
            
        } catch (Exception e) {
            // Si quelque chose plante (ex: erreur SQL), on renvoie une erreur claire
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    
    

    /**
     * Calcule le portefeuille du médecin (50% des prestations payées)
     */
    @GetMapping("/{id}/portefeuille")
    public ResponseEntity<?> getPortefeuille(@PathVariable Long id) {
        try {
            // 1. Appel au Repository pour la somme SQL SUM(prix_applique)
            Double totalBrut = prestationRepository.calculerTotalBrut(id);
            
            // Sécurité : si pas encore de revenus, on évite le NullPointerException
            if (totalBrut == null) totalBrut = 0.0;

            // 2. Application de la règle métier (50/50)
            Double partMedecin = totalBrut * 0.5;
            Double partClinique = totalBrut * 0.5;

            // 3. On renvoie un objet clair pour FastAPI
            return ResponseEntity.ok(Map.of(
                "medecinId", id,
                "totalEncaisse", totalBrut,
                "maPart", partMedecin,
                "commissionClinique", partClinique,
                "devise", "Ar"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erreur lors du calcul financier : " + e.getMessage()
            ));
        }
    }
}