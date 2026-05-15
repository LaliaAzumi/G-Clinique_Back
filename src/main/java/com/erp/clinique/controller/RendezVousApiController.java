package com.erp.clinique.controller;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.model.Patient;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.repository.ActeMedicalRepository;
import com.erp.clinique.repository.PrestationRepository;
import com.erp.clinique.repository.RendezVousRepository;
import com.erp.clinique.repository.ConsultationRepository;
import com.erp.clinique.repository.MedicamentRepository;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.OrdonnanceRepository;
import com.erp.clinique.repository.UserRepository;
import com.erp.clinique.service.RendezVousService;
import com.erp.clinique.service.OrdonnanceService;
import com.erp.clinique.service.EmailService;
import com.erp.clinique.service.NotificationService;
import com.erp.clinique.model.Prestation;
import com.erp.clinique.model.ActeMedical;
import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Users;


// Pour l'annotation @Transactional
import org.springframework.transaction.annotation.Transactional;

// Pour les Entités (Vérifiez bien que le package est com.erp.clinique.model)
import com.erp.clinique.model.Consultation;
import com.erp.clinique.model.Ordonnance;
import com.erp.clinique.model.Prescription;
import com.erp.clinique.model.Medicament;

// Pour manipuler les fichiers (Ordonnance PDF)
import java.io.File;

@RestController
@RequestMapping("/api/v1/rendez-vous")
public class RendezVousApiController {

    @Autowired
    private RendezVousService rendezVousService;
    @Autowired
    private PrestationRepository prestationRepository;
    @Autowired
    private ActeMedicalRepository acteMedicalRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private MedicamentRepository medicamentRepository;

    @Autowired
    private OrdonnanceRepository ordonnanceRepository;

    @Autowired
    private OrdonnanceService ordonnanceService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedecinUserRepository medecinUserRepository;

    private String rdvLabel(RendezVous rdv) {
        return String.format(
            "%s %s le %s a %s",
            rdv.getPatient().getNom(),
            rdv.getPatient().getPrenom(),
            rdv.getDate(),
            rdv.getHeure()
        );
    }

    private void notifySecretaires(String message) {
        notificationService.sendNotificationToSecretaires(message);
        for (Users secretaire : userRepository.findByRole("SECRETAIRE")) {
            notificationService.sendNotificationToUser(secretaire.getId(), message);
        }
    }

    private void notifyMedecin(RendezVous rdv, String message) {
        medecinUserRepository.findByMedecinId(rdv.getMedecin().getId())
            .map(MedecinUser::getUserId)
            .ifPresent(userId -> notificationService.sendNotificationToUser(userId, message));
    }

    private void sendPatientEmail(RendezVous rdv, String subject, String body) {
        try {
            emailService.sendRendezVousEmail(rdv.getPatient().getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Erreur email patient: " + e.getMessage());
        }
    }

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
            String message = "Nouveau rendez-vous public a valider: " + rdvLabel(rdv);
            notifySecretaires(message);
            sendPatientEmail(
                rdv,
                "Votre demande de rendez-vous - G-Clinique",
                "Bonjour " + rdv.getPatient().getPrenom() + ",\n\n"
                    + "Votre demande de rendez-vous du " + rdv.getDate() + " a " + rdv.getHeure()
                    + " a ete enregistree. Une secretaire validera votre paiement.\n\n"
                    + "Cordialement,\nG-Clinique"
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
                String notification = "Paiement valide, rendez-vous confirme: " + rdvLabel(rdv);
                notifyMedecin(rdv, notification);
                notifySecretaires(notification);
                sendPatientEmail(
                    rdv,
                    "Rendez-vous confirme - G-Clinique",
                    "Bonjour " + rdv.getPatient().getPrenom() + ",\n\n"
                        + "Votre paiement a ete valide. Votre rendez-vous est confirme pour le "
                        + rdv.getDate() + " a " + rdv.getHeure() + ".\n\n"
                        + "Cordialement,\nG-Clinique"
                );
                
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
    

    //annule rdv non payer par secretaire
    @PutMapping("/{id}/annuler")
    public ResponseEntity<RendezVous> annuler(@PathVariable Long id) {
        RendezVous rdv = rendezVousService.findById(id)
            .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));

        rdv.setStatut("ANNULE");
        // sécurité null + comparaison safe
        if ("EN_ATTENTE_PAIEMENT".equals(rdv.getStatutPaiement())) {

            rdv.setStatutPaiement("REFUSE");

            if (rdv.getPaiement() != null) {
                rdv.getPaiement().setStatut("REFUSE");
            }
        }

        // 🔥 SAUVEGARDER LES CHANGEMENTS
        rendezVousService.save(rdv);
        String message = "Rendez-vous annule: " + rdvLabel(rdv);
        notifyMedecin(rdv, message);
        notifySecretaires(message);
        sendPatientEmail(
            rdv,
            "Rendez-vous annule - G-Clinique",
            "Bonjour " + rdv.getPatient().getPrenom() + ",\n\n"
                + "Votre rendez-vous du " + rdv.getDate() + " a " + rdv.getHeure()
                + " a ete annule.\n\nCordialement,\nG-Clinique"
        );
        
        return ResponseEntity.ok(rdv);
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


    //create rdv par le secretaire
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Map<String, Object> data) {
        try {
            RendezVous rdv = rendezVousService.createSimple(
                Long.valueOf(data.get("patientId").toString()),
                Long.valueOf(data.get("medecinId").toString()),
                LocalDate.parse(data.get("date").toString()),
                LocalTime.parse(data.get("heure").toString()),
                (String) data.get("motif")
            );
            String message = "Nouveau rendez-vous planifie: " + rdvLabel(rdv);
            notifyMedecin(rdv, message);
            sendPatientEmail(
                rdv,
                "Rendez-vous planifie - G-Clinique",
                "Bonjour " + rdv.getPatient().getPrenom() + ",\n\n"
                    + "Votre rendez-vous est planifie pour le " + rdv.getDate()
                    + " a " + rdv.getHeure() + ".\n\nCordialement,\nG-Clinique"
            );

            return ResponseEntity.ok(rdv);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    //update rdv par le secretaire
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            RendezVous rdv = rendezVousService.updateSimple(id, data);
            String message = "Rendez-vous modifie: " + rdvLabel(rdv) + " - statut " + rdv.getStatut();
            notifyMedecin(rdv, message);
            notifySecretaires(message);
            sendPatientEmail(
                rdv,
                "Modification de votre rendez-vous - G-Clinique",
                "Bonjour " + rdv.getPatient().getPrenom() + ",\n\n"
                    + "Votre rendez-vous est maintenant prevu le " + rdv.getDate()
                    + " a " + rdv.getHeure() + ". Statut: " + rdv.getStatut()
                    + ".\n\nCordialement,\nG-Clinique"
            );
            return ResponseEntity.ok(rdv);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // @PutMapping("/{id}/prestations")
    // public ResponseEntity<?> updatePrestations(@PathVariable Long id, @RequestBody Map<String, Object> data) {
    //     try {
    //         if (!rendezVousService.findById(id).isPresent()) {
    //             return ResponseEntity.notFound().build();
    //         }

    //         Object prestationsObject = data.get("prestations");
    //         if (prestationsObject instanceof java.util.List) {
    //             @SuppressWarnings("unchecked")
    //             java.util.List<Map<String, Object>> prestations = (java.util.List<Map<String, Object>>) prestationsObject;
    //             for (Map<String, Object> prestationData : prestations) {
    //                 if (prestationData.get("id") == null) continue;
    //                 Long prestationId = Long.valueOf(prestationData.get("id").toString());
    //                 String resultat = prestationData.get("resultat") != null ? prestationData.get("resultat").toString() : null;
    //                 prestationRepository.findById(prestationId).ifPresent(prestation -> {
    //                     prestation.setResultat(resultat);
    //                     prestationRepository.save(prestation);
    //                 });
    //             }
    //         }

    //         Object newActeIdsObject = data.get("newActeIds");
    //         if (newActeIdsObject instanceof java.util.List) {
    //             @SuppressWarnings("unchecked")
    //             java.util.List<Object> rawIds = (java.util.List<Object>) newActeIdsObject;
    //             for (Object rawId : rawIds) {
    //                 if (rawId == null) continue;
    //                 Long acteId = Long.valueOf(rawId.toString());
    //                 rendezVousService.findById(id).ifPresent(rdv -> {
    //                     acteMedicalRepository.findById(acteId).ifPresent(acte -> {
    //                         Prestation prestation = new Prestation(rdv, acte, acte.getPrix());
    //                         prestationRepository.save(prestation);
    //                     });
    //                 });
    //             }
    //         }

    //         return ResponseEntity.ok(Map.of("message", "Prestations mises à jour"));
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    //     }
    // }

@PutMapping("/{id}/prestations")
@Transactional
public ResponseEntity<?> updatePrestations(@PathVariable Long id, @RequestBody Map<String, Object> requestBody) {
    return rendezVousRepository.findById(id).map(rdv -> {
        try {
            // --- 1. MISE À JOUR DES PRESTATIONS EXISTANTES ---
            if (requestBody.containsKey("prestations")) {
                List<Map<String, Object>> prestationsData = (List<Map<String, Object>>) requestBody.get("prestations");
                for (Map<String, Object> pData : prestationsData) {
                    Long pId = Long.valueOf(pData.get("id").toString());
                    String res = (String) pData.get("resultat");
                    rdv.getPrestations().stream()
                        .filter(p -> p.getId().equals(pId))
                        .findFirst()
                        .ifPresent(p -> p.setResultat(res));
                }
            }

            // --- 2. AJOUT DES NOUVEAUX ACTES ---
            if (requestBody.containsKey("newActeIds")) {
                List<Integer> acteIds = (List<Integer>) requestBody.get("newActeIds");

                for (Integer acteId : acteIds) {
                    // if (acteId == null) continue;
                    if (acteId == null || acteId == 1) {
                            continue; // On passe à l'itération suivante sans rien insérer
                    }
                    ActeMedical acte = acteMedicalRepository.findById(Long.valueOf(acteId))
                        .orElseThrow(() -> new RuntimeException("Acte introuvable"));

                    Prestation prestation = new Prestation();
                    prestation.setActe(acte);
                    prestation.setPrixApplique(acte.getPrix()); // 💥 IMPORTANT
                    prestation.setRendezvous(rdv);

                    rdv.getPrestations().add(prestation);
                }
            }

            // --- 2. CRÉATION DE LA CONSULTATION (EXAMEN) ---
            Consultation consultation = new Consultation();
            if (requestBody.containsKey("vitals")) {
                Map<String, Object> vitals = (Map<String, Object>) requestBody.get("vitals");
                String maladie = String.valueOf(vitals.getOrDefault("maladie", ""));
                
                // Concaténation avec les "/"
                String diagComplet = String.format("%s / Temp: %s / Tension: %s / Pouls: %s / Sat: %s / Poids: %s / Obs: %s",
                        maladie, vitals.get("temperature"), vitals.get("tension"), 
                        vitals.get("pouls"), vitals.get("saturation"), vitals.get("poids"), vitals.get("observations"));

                consultation.setRendezVous(rdv);
                consultation.setDate(LocalDate.now());
                consultation.setMaladie(maladie);
                consultation.setDiagnostique(diagComplet);
                consultation = consultationRepository.save(consultation);
            }

            // --- 3. CRÉATION DE L'ORDONNANCE (Si des prescriptions existent) ---
            // On suppose que ton frontend envoie aussi une liste "prescriptions"
            if (requestBody.containsKey("prescriptions")) {
                List<Map<String, Object>> prescriptionsData = (List<Map<String, Object>>) requestBody.get("prescriptions");
                
                if (!prescriptionsData.isEmpty()) {
                    Ordonnance ordonnance = new Ordonnance();
                    ordonnance.setConsultation(consultation);

                    for (Map<String, Object> pMap : prescriptionsData) {
                        Long medId = Long.valueOf(pMap.get("medicamentId").toString());
                        Medicament medicament = medicamentRepository.findById(medId).orElseThrow();
                        
                        int qte = Integer.parseInt(pMap.get("quantite").toString());
                        medicament.setQStock(medicament.getQStock() - qte); // Mise à jour stock

                        Prescription p = new Prescription();
                        p.setMedicament(medicament);
                        p.setPosologie((String) pMap.get("posologie"));
                        p.setDuree((String) pMap.get("duree"));
                        p.setQuantite(qte);
                        p.setOrdonnance(ordonnance);
                        ordonnance.getPrescriptions().add(p);
                    }

                    ordonnanceRepository.save(ordonnance);

                    // --- 4. GÉNÉRATION PDF ET ENVOI EMAIL ---
                    try {
                        String folder = "/src/main/resources/static/pdf_ordonnances/"; // Ou ton chemin local
                        File dir = new File(folder);
                        if (!dir.exists()) dir.mkdirs();

                        File pdfFile = new File(dir, "ordonnance_" + ordonnance.getId() + ".pdf");
                        ordonnanceService.generateOrdonnancePdf(ordonnance, pdfFile);
                        
                        ordonnance.setPdfPath(pdfFile.getAbsolutePath());
                        ordonnanceRepository.save(ordonnance);

                        // Envoi Mail
                        String subject = "Votre ordonnance - Clinique";
                        String body = "Bonjour, voici votre ordonnance en pièce jointe.";
                        emailService.sendPdfEmail(rdv.getPatient().getEmail(), subject, body, pdfFile);
                        
                    } catch (Exception e) {
                        System.err.println("Erreur PDF/Email: " + e.getMessage());
                        // On ne bloque pas la transaction pour l'email
                    }
                }
            }

            rdv.setStatut("TERMINE");
            rendezVousRepository.save(rdv);
            String message = "Consultation terminee: " + rdvLabel(rdv);
            notifyMedecin(rdv, message);
            notifySecretaires(message);
            sendPatientEmail(
                rdv,
                "Consultation terminee - G-Clinique",
                "Bonjour " + rdv.getPatient().getPrenom() + ",\n\n"
                    + "Votre consultation du " + rdv.getDate() + " a ete marquee comme terminee."
                    + "\n\nCordialement,\nG-Clinique"
            );

            return ResponseEntity.ok(Map.of("message", "Tout a été enregistré et l'email a été envoyé"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }).orElse(ResponseEntity.notFound().build());
}

    //delete rdv par le secretaire
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            return rendezVousService.findById(id).map(rdv -> {
                rendezVousService.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Rendez-vous supprimé"));
            }).orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    //lister les rdv avec paiement
    @GetMapping("/paiements")
    public ResponseEntity<List<RendezVous>> getRdvWithPaiement() {
        return ResponseEntity.ok(
            rendezVousService.findAllWithPaiement()
        );
    }

    //MEDECIN REPORTER RDV
    @PutMapping("/{id}/reporter")
    public ResponseEntity<?> reporterRdv(@PathVariable Long id) {
        RendezVous rdv = rendezVousService.reporterRdv(id);
        String message = "Rendez-vous a reprogrammer par le secretaire: " + rdvLabel(rdv);
        notifySecretaires(message);
        sendPatientEmail(
            rdv,
            "Rendez-vous en attente de report - G-Clinique",
            "Bonjour " + rdv.getPatient().getPrenom() + ",\n\n"
                + "Votre rendez-vous du " + rdv.getDate() + " a " + rdv.getHeure()
                + " doit etre reprogramme. Une secretaire vous recontactera.\n\n"
                + "Cordialement,\nG-Clinique"
        );
        return ResponseEntity.ok(rdv);
    }
}
