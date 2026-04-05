package com.erp.clinique.controller;

import com.erp.clinique.dto.ApiResponse;
import com.erp.clinique.service.FastApiAuthService;
import com.erp.clinique.service.FastApiUserService;
import com.erp.clinique.service.EmailService;
import com.erp.clinique.utils.MdpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API REST pour la gestion des secrétaires via le frontend React
 */
@RestController
@RequestMapping("/api/v1/secretaires")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class SecretaireApiController {

    @Autowired
    private FastApiUserService fastApiUserService;

    @Autowired
    private FastApiAuthService fastApiAuthService;

    @Autowired
    private EmailService emailService; // Injecté pour l'envoi du mot de passe par mail

    /**
     * Crée un compte secrétaire
     * Seuls le username et l'email sont requis dans le corps de la requête
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createSecretaire(
            @RequestBody Map<String, String> userData,
            @RequestHeader("Authorization") String authHeader) {

        // 1. Vérification de la présence du Token
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

        // 2. Vérification du rôle ADMIN (Sécurité)
        String role = (String) tokenData.get("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403)
                .body(new ApiResponse(false, "Accès réservé aux administrateurs", null));
        }

        // 3. Récupération des données simplifiées (Nom et Email uniquement)
        String username = userData.get("username");
        String email = userData.get("email");

        if (username == null || email == null || username.isEmpty() || email.isEmpty()) {
            return ResponseEntity.status(400)
                .body(new ApiResponse(false, "Le nom d'utilisateur et l'email sont obligatoires", null));
        }

        // 4. Génération automatique du mot de passe par défaut
        String randomPassword = MdpUtils.generateRandomMdp();

        // 5. Création de l'utilisateur dans le microservice FastAPI avec le rôle SECRETAIRE
        Map<String, Object> result = fastApiUserService.createUser(
            username,
            email,
            randomPassword,
            "SECRETAIRE"
        );

        if (result == null) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur lors de la création via le microservice FastAPI", null));
        }

        // 6. Envoi de l'email avec les identifiants au secrétaire
        try {
            emailService.sendPasswordEmail(email, username, randomPassword);
        } catch (Exception e) {
            // On log l'erreur mais on ne bloque pas la réponse car l'utilisateur est déjà créé
            System.err.println("Erreur d'envoi d'email : " + e.getMessage());
        }

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Secrétaire créé avec succès. Un email contenant le mot de passe a été envoyé à " + email,
            result
        ));
    }

    /**
     * Liste tous les utilisateurs ayant le rôle SECRETAIRE
     */
    @GetMapping
    public ResponseEntity<ApiResponse> listSecretaires() {
        Map<String, Object> users = fastApiUserService.listUsers("SECRETAIRE");

        if (users == null) {
            return ResponseEntity.status(503)
                .body(new ApiResponse(false, "Le service de gestion des utilisateurs est indisponible", null));
        }

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Liste des secrétaires récupérée avec succès",
            users
        ));
    }
}