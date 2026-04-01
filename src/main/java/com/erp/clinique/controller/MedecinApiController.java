package com.erp.clinique.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.dto.ApiResponse;
import com.erp.clinique.model.Medecin;
import com.erp.clinique.repository.PrestationRepository;
import com.erp.clinique.service.FastApiAuthService;
import com.erp.clinique.service.MedecinFastApiService;
import com.erp.clinique.service.MedecinService;

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
     */
    @GetMapping
    public ResponseEntity<ApiResponse> listMedecins() {
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Liste des médecins",
            Map.of("medecins", medecinService.findAll())
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
}
