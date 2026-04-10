package com.erp.clinique.controller;

import com.erp.clinique.dto.ApiResponse;
import com.erp.clinique.model.Users;
import com.erp.clinique.service.FastApiAuthService;
import com.erp.clinique.service.FastApiUserService;
import com.erp.clinique.service.UserService;
import com.erp.clinique.utils.MdpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API REST pour la gestion des secrétaires via FastAPI
 */
@RestController
@RequestMapping("/api/v1/secretaires")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SecretaireApiController {

    @Autowired
    private FastApiUserService fastApiUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private FastApiAuthService fastApiAuthService;

    /**
     * Crée un compte secrétaire via FastAPI
     * Nécessite un token JWT admin
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createSecretaire(
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

        // Vérifie le rôle admin
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

        // Génère un mot de passe aléatoire
        String randomPassword = MdpUtils.generateRandomMdp();

        // Crée l'utilisateur via FastAPI
        Map<String, Object> result = fastApiUserService.createUser(
            username,
            email,
            randomPassword,
            "SECRETAIRE"
        );

        if (result == null) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur lors de la création via FastAPI", null));
        }

        // Ajoute le mot de passe à la réponse (pour l'admin)
        result.put("generatedPassword", randomPassword);

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Secrétaire créé avec succès",
            result
        ));
    }

    /**
     * Liste tous les secrétaires
     */
    @GetMapping
    public ResponseEntity<ApiResponse> listSecretaires() {
        Map<String, Object> users = fastApiUserService.listUsers("SECRETAIRE");

        if (users == null) {
            return ResponseEntity.status(503)
                .body(new ApiResponse(false, "Service FastAPI indisponible", null));
        }

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Liste des secrétaires",
            users
        ));
    }

    /**
     * Modifie un secrétaire (Accès ADMIN uniquement)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateSecretaire(
            @PathVariable Long id,
            @RequestBody Map<String, String> userData,
            @RequestHeader("Authorization") String authHeader) {

        // Vérifie le token
        ResponseEntity<ApiResponse> authError = checkAdminAuth(authHeader);
        if (authError != null) return authError;

        String username = userData.get("username");
        String email = userData.get("email");

        if (username == null || email == null) {
            return ResponseEntity.status(400)
                .body(new ApiResponse(false, "Username et email requis", null));
        }

        try {
            // Met à jour l'utilisateur via FastAPI
            Map<String, Object> result = fastApiUserService.updateUser(id, username, email, "SECRETAIRE");

            if (result == null) {
                return ResponseEntity.status(500)
                    .body(new ApiResponse(false, "Erreur lors de la modification via FastAPI", null));
            }

            return ResponseEntity.ok(new ApiResponse(
                true,
                "Secrétaire modifié avec succès",
                result
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur: " + e.getMessage(), null));
        }
    }

    /**
     * Supprime un secrétaire (Accès ADMIN uniquement)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteSecretaire(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        // Vérifie le token
        ResponseEntity<ApiResponse> authError = checkAdminAuth(authHeader);
        if (authError != null) return authError;

        try {
            // Supprime l'utilisateur via FastAPI
            boolean deleted = fastApiUserService.deleteUser(id);

            if (!deleted) {
                return ResponseEntity.status(500)
                    .body(new ApiResponse(false, "Erreur lors de la suppression via FastAPI", null));
            }

            return ResponseEntity.ok(new ApiResponse(
                true,
                "Secrétaire supprimé avec succès",
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur: " + e.getMessage(), null));
        }
    }

    /**
     * Méthode utilitaire pour vérifier si l'utilisateur est ADMIN
     */
    private ResponseEntity<ApiResponse> checkAdminAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Token manquant", null));
        }

        String token = authHeader.substring(7);
        Map<String, Object> tokenData = fastApiAuthService.validateToken(token);

        if (tokenData == null) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Token invalide", null));
        }

        String role = (String) tokenData.get("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body(new ApiResponse(false, "Accès réservé aux administrateurs", null));
        }

        return null; // Tout est OK
    }
}
