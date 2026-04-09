package com.erp.clinique.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.dto.ApiResponse;
import com.erp.clinique.model.Medecin;
import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.repository.MedecinRepository;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.PrestationRepository;
import com.erp.clinique.repository.UserRepository;
import com.erp.clinique.service.FastApiAuthService;
import com.erp.clinique.service.MedecinFastApiService;
import com.erp.clinique.service.MedecinService;
import com.erp.clinique.service.UserService;

/**
 * API REST pour la gestion des médecins via FastAPI
 * Endpoints pour créer des médecins avec leur compte utilisateur
 */
@RestController
@RequestMapping("/api/v1/medecins")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MedecinApiController {

    @Autowired
    private MedecinFastApiService medecinFastApiService;

    @Autowired
    private MedecinService medecinService;

    @Autowired
    private FastApiAuthService fastApiAuthService;
    
    @Autowired
    private PrestationRepository prestationRepository;
    
    @Autowired
    private UserService userService;
    @Autowired
    private MedecinRepository medecinRepository;
    @Autowired
    private MedecinUserRepository medecinUserRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * Crée un médecin avec son compte utilisateur via FastAPI
     * Nécessite un token JWT admin
     */
    @PostMapping("/create-with-user")
    public ResponseEntity<ApiResponse> createMedecinWithUser(
            @RequestBody Map<String, Object> requestData,
            @RequestHeader("Authorization") String authHeader) {

        // Vérifie le token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Token manquant", null));
        }

        String token = authHeader.substring(7);
        Map<String, Object> tokenData = fastApiAuthService.validateToken(token);

        if (tokenData == null) {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Token invalide", null));
        }

        // Vérifie le rôle admin
        String role = (String) tokenData.get("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403)
                .body(new ApiResponse(false, "Accès réservé aux administrateurs", null));
        }

        // Extrait les données du médecin
        @SuppressWarnings("unchecked")
        Map<String, String> medecinData = (Map<String, String>) requestData.get("medecin");
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");

        if (medecinData == null || username == null || email == null) {
            return ResponseEntity.status(400)
                .body(new ApiResponse(false, "Données manquantes (medecin, username, email)", null));
        }

        // Crée l'objet Medecin
        Medecin medecin = new Medecin();
        medecin.setNom(medecinData.get("nom"));
        medecin.setSpecialite(medecinData.get("specialite"));
        medecin.setTelephone(medecinData.get("telephone"));
        medecin.setAdresse(medecinData.get("adresse"));

        // Crée le médecin avec son user via FastAPI
        Map<String, Object> result = medecinFastApiService.createMedecinWithUser(medecin, username, email);

        if (result == null) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur lors de la création via le microservice FastAPI", null));
        }

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Médecin et compte utilisateur créés avec succès",
            result
        ));
    }

    /**
     * Crée uniquement le compte utilisateur pour un médecin existant
     */
    @PostMapping("/{medecinId}/create-user")
    public ResponseEntity<ApiResponse> createUserForMedecin(
            @PathVariable Long medecinId,
            @RequestBody Map<String, String> userData,
            @RequestHeader("Authorization") String authHeader) {

        // Vérifie le token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Token manquant", null));
        }

        String token = authHeader.substring(7);
        Map<String, Object> tokenData = fastApiAuthService.validateToken(token);

        if (tokenData == null) {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Token invalide", null));
        }

        // Vérifie le rôle
        String role = (String) tokenData.get("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403)
                .body(new ApiResponse(false, "Accès réservé aux administrateurs", null));
        }

        String username = userData.get("username");
        String email = userData.get("email");

        if (username == null || email == null) {
            return ResponseEntity.status(400)
                .body(new ApiResponse(false, "Username et email requis", null));
        }

        boolean success = medecinFastApiService.createUserForExistingMedecin(medecinId, username, email);

        if (!success) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur lors de la création du compte", null));
        }

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Compte utilisateur créé pour le médecin",
            Map.of("medecinId", medecinId, "username", username)
        ));
    }

    /**
     * Liste tous les médecins
     
    @GetMapping
    public ResponseEntity<ApiResponse> listMedecins() {
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Liste des médecins",
            Map.of("medecins", medecinService.findAll())
        ));
    }*/
    @GetMapping
    public ResponseEntity<ApiResponse> listMedecins() {
        // On appelle la méthode enrichie au lieu du findAll() basique
        List<Map<String, Object>> medecinsEnrichis = medecinService.findAllEnriched();

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Liste des médecins avec détails",
            Map.of("medecins", medecinsEnrichis)
        ));
    }

    /**
     * Récupère un médecin par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getMedecin(@PathVariable Long id) {
        return medecinService.findById(id)
            .map(medecin -> ResponseEntity.ok(new ApiResponse(
                true,
                "Médecin trouvé",
                Map.of("medecin", medecin)
            )))
            .orElseGet(() -> ResponseEntity.status(404)
                .body(new ApiResponse(false, "Médecin non trouvé", null)));
    }

    /**
     * Récupère le portefeuille financier du médecin (50% des prestations payées)
     */
    @GetMapping("/{id}/portefeuille")
    public ResponseEntity<ApiResponse> getPortefeuille(@PathVariable Long id) {
        try {
            // 1. Calcul de la somme totale brute via le Repository
            Double totalBrut = prestationRepository.calculerTotalBrut(id);
            
            if (totalBrut == null) {
                totalBrut = 0.0;
            }

            // 2. Calcul de la part du médecin (50%)
            Double partMedecin = totalBrut * 0.5;
            Double partClinique = totalBrut * 0.5;

            // 3. Retourne la réponse formatée
            Map<String, Object> data = Map.of(
                "totalEncaisse", totalBrut,
                "maPart", partMedecin,
                "commissionClinique", partClinique,
                "devise", "Ar"
            );

            return ResponseEntity.ok(new ApiResponse(
                true, 
                "Portefeuille récupéré avec succès", 
                data
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur lors du calcul financier : " + e.getMessage(), null));
        }
    }
    
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateFullMedecin(
            @PathVariable Long id, // On garde l'id de l'URL par convention, mais on va utiliser medecinId du body
            @RequestBody Map<String, Object> requestData,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Token manquant", null));
        }

        try {
            // --- RÉCUPÉRATION DU BON ID ---
            // Dans tes logs, selectedMedecin.id est 3, mais medecinId est 5.
            // On récupère le 5 qui est dans le corps de la requête.
            Object mIdObj = requestData.get("medecinId");
            if (mIdObj == null) {
                return ResponseEntity.status(400).body(new ApiResponse(false, "Le champ medecinId est manquant dans les données", null));
            }
            
            Long realMedecinId = Long.valueOf(mIdObj.toString());
            System.out.println("Mise à jour pour le Médecin ID réel : " + realMedecinId);

            // 2. Recherche du médecin avec le BON ID (5)
            Medecin medecin = medecinService.findById(realMedecinId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable avec l'ID : " + realMedecinId));
                
            medecin.setNom((String) requestData.get("nom"));
            medecin.setSpecialite((String) requestData.get("specialite"));
            medecin.setTelephone((String) requestData.get("telephone"));
            medecin.setAdresse((String) requestData.get("adresse"));
            medecinService.save(medecin);

            // 3. Mise à jour Utilisateur (FastAPI / Local)
            // On récupère le userId (qui est 27 dans ton log)
            Object uIdObj = requestData.get("userId");
            if (uIdObj != null) {
                Long userId = Long.valueOf(uIdObj.toString());
                String newUsername = (String) requestData.get("username");
                String newEmail = (String) requestData.get("email");

                userService.findById(userId).ifPresent(u -> {
                    // Vérification email doublon
                    if (newEmail != null && !newEmail.equalsIgnoreCase(u.getEmail())) {
                        if (userService.findByEmail(newEmail).isEmpty()) {
                            u.setEmail(newEmail);
                        }
                    }
                    if (newUsername != null && !newUsername.equals(u.getUsername())) {
                        u.setUsername(newUsername);
                    }
                    userService.save(u);
                });
            }

            return ResponseEntity.ok(new ApiResponse(true, "Mise à jour réussie !", medecin));

        } catch (Exception e) {
            System.out.println("ERREUR MISE A JOUR : " + e.getMessage());
            return ResponseEntity.status(500).body(new ApiResponse(false, "Erreur : " + e.getMessage(), null));
        }
    }
    /**
     * Supprime un médecin (Admin uniquement)
     */
    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse> deleteMedecin(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        // 1. Validation du token (ton code habituel)
        String token = authHeader.substring(7);
        Map<String, Object> tokenData = fastApiAuthService.validateToken(token);
        if (tokenData == null || !"ADMIN".equals(tokenData.get("role"))) {
            return ResponseEntity.status(403).body(new ApiResponse(false, "Accès refusé", null));
        }

        try {
            // 2. Récupérer la relation pour trouver le userId
            java.util.Optional<MedecinUser> muOpt = medecinUserRepository.findByMedecinId(id);

            if (muOpt.isPresent()) {
                MedecinUser mu = muOpt.get();
                Long uId = mu.getUserId();

                // 3. Supprimer la ligne dans MedecinUser (la table de liaison)
                medecinUserRepository.delete(mu);

                // 4. Supprimer l'utilisateur dans la table User
                // Utilise ton userService ou userRepository selon tes injections
                userRepository.deleteById(uId); 
            }

            // 5. Supprimer le médecin dans la table Medecin
            boolean deleted = medecinService.deleteById(id);

            if (!deleted) {
                return ResponseEntity.status(404).body(new ApiResponse(false, "Médecin non trouvé", null));
            }

            return ResponseEntity.ok(new ApiResponse(true, "Nettoyage complet effectué avec succès", null));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse(false, "Erreur : " + e.getMessage(), null));
        }
    }
}
