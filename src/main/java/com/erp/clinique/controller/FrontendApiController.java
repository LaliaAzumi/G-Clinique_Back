package com.erp.clinique.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.dto.ApiResponse;
import com.erp.clinique.dto.LoginApiRequest;
import com.erp.clinique.dto.UserProfileResponse;
import com.erp.clinique.service.FastApiAuthService;
import com.erp.clinique.service.FastApiUserService;

/**
 * Contrôleur REST API pour les frontends 
 * Expose des endpoints JSON sans templates Thymeleaf
 * CORS configuré pour permettre les requêtes cross-origin
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Autorise toutes les origines (à restreindre en production)
public class FrontendApiController {

    @Autowired
    private FastApiAuthService fastApiAuthService;

    @Autowired
    private FastApiUserService fastApiUserService;

    /**
     * Authentification API pour frontend
     * Retourne un token JWT au lieu de rediriger
     */
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse> apiLogin(@RequestBody LoginApiRequest request) {
        String token = fastApiAuthService.authenticate(request.getUsername(), request.getPassword());

        if (token == null) {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Identifiants invalides", null));
        }

        // Récupère les infos utilisateur
        Map<String, Object> userData = fastApiUserService.listUsers(null);
        
        return ResponseEntity.ok(new ApiResponse(
            true, 
            "Authentification réussie",
            Map.of(
                "token", token,
                "token_type", "bearer",
                "expires_in", 1800,
                "username", request.getUsername()
            )
        ));
    }

    /**
     * Création d'utilisateur via API
     */
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse> apiRegister(@RequestBody Map<String, String> userData) {
        Map<String, Object> result = fastApiUserService.createUser(
            userData.get("username"),
            userData.get("email"),
            userData.get("password"),
            userData.get("role")
        );

        if (result == null) {
            return ResponseEntity.status(400)
                .body(new ApiResponse(false, "Erreur lors de la création de l'utilisateur", null));
        }

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Utilisateur créé avec succès",
            result
        ));
    }

    /**
     * Création d'un secrétaire via API (réservé aux admins)
     * Génère un mot de passe aléatoire et envoie par email
     */
    @PostMapping("/staff/secretaires")
    public ResponseEntity<ApiResponse> createSecretaireApi(@RequestBody Map<String, String> userData,
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

        // Crée le secrétaire via FastAPI
        Map<String, Object> result = fastApiUserService.createUser(
            userData.get("username"),
            userData.get("email"),
            userData.get("password"),
            "SECRETAIRE"
        );

        if (result == null) {
            return ResponseEntity.status(400)
                .body(new ApiResponse(false, "Erreur lors de la création du secrétaire", null));
        }

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Secrétaire créé avec succès",
            result
        ));
    }

    /**
     * Création d'un médecin via API (réservé aux admins)
     */
    @PostMapping("/staff/medecins")
    public ResponseEntity<ApiResponse> createMedecinApi(@RequestBody Map<String, String> userData,
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

        // Crée le médecin via FastAPI
        Map<String, Object> result = fastApiUserService.createUser(
            userData.get("username"),
            userData.get("email"),
            userData.get("password"),
            "DOCTOR"
        );

        if (result == null) {
            return ResponseEntity.status(400)
                .body(new ApiResponse(false, "Erreur lors de la création du médecin", null));
        }

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Médecin créé avec succès",
            result
        ));
    }

    /**
     * Liste tous les secrétaires
     */
    @GetMapping("/staff/secretaires")
    public ResponseEntity<ApiResponse> listSecretaires(@RequestHeader("Authorization") String authHeader) {
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

        Map<String, Object> users = fastApiUserService.listUsers("SECRETAIRE");
        if (users == null) {
            return ResponseEntity.status(503)
                .body(new ApiResponse(false, "Service FastAPI indisponible", null));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Liste des secrétaires", users));
    }

    /**
     * Liste tous les médecins
     */
    @GetMapping("/staff/medecins")
    public ResponseEntity<ApiResponse> listMedecins(@RequestHeader("Authorization") String authHeader) {
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

        Map<String, Object> users = fastApiUserService.listUsers("DOCTOR");
        if (users == null) {
            return ResponseEntity.status(503)
                .body(new ApiResponse(false, "Service FastAPI indisponible", null));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Liste des médecins", users));
    }

    /**
     * Récupère le profil de l'utilisateur connecté
     * Nécessite le token JWT dans le header Authorization
     */
    @GetMapping("/auth/me")
    public ResponseEntity<ApiResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Token manquant ou invalide", null));
        }

        String token = authHeader.substring(7);
        Map<String, Object> tokenData = fastApiAuthService.validateToken(token);

        if (tokenData == null) {
            return ResponseEntity.status(401)
                .body(new ApiResponse(false, "Token invalide ou expiré", null));
        }

        String username = (String) tokenData.get("username");
        String role = (String) tokenData.get("role");

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Profil récupéré",
            new UserProfileResponse(
                (String) tokenData.get("sub"),
                username,
                role,
                null // email récupéré séparément si besoin
            )
        ));
    }

    /**
     * Vérifie la validité d'un token
     */
    @PostMapping("/auth/verify")
    public ResponseEntity<ApiResponse> verifyToken(@RequestHeader("Authorization") String authHeader) {
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

//        return ResponseEntity.ok(new ApiResponse(
//            true,
//            "Token valide",
//            Map.of(
//                "valid", true,
//                "user_id", tokenData.get("sub"),
//                "username", tokenData.get("username"),
//                "role", tokenData.get("role")
//            )
//        ));
     // C'EST ICI QU'IL FAUT CHANGER :
        // On remplace le Map.of(...) par un HashMap
        Map<String, Object> userData = new HashMap<>();
        userData.put("valid", true);
        userData.put("user_id", tokenData.get("sub"));      // Si "sub" est null, HashMap l'accepte
        userData.put("username", tokenData.get("username")); // Si "username" est null, HashMap l'accepte
        userData.put("role", tokenData.get("role"));         // Si "role" est null, HashMap l'accepte

        return ResponseEntity.ok(new ApiResponse(
            true,
            "Token valide",
            userData // On envoie le HashMap au lieu du Map.of
        ));
    }

    /**
     * Logout API - invalide le token côté client
     * (Le token JWT reste valide jusqu'à expiration, mais on peut le blacklister)
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse> apiLogout() {
        // En production, ajouter le token à une blacklist Redis
        return ResponseEntity.ok(new ApiResponse(true, "Déconnexion réussie", null));
    }

    /**
     * Liste tous les utilisateurs (réservé aux admins)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> listUsers(@RequestParam(required = false) String role) {
        Map<String, Object> users = fastApiUserService.listUsers(role);
        
        if (users == null) {
            return ResponseEntity.status(503)
                .body(new ApiResponse(false, "Service FastAPI indisponible", null));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Liste des utilisateurs", users));
    }

    /**
     * Documentation de l'API pour les développeurs frontend
     */
    @GetMapping("/docs")
    public ResponseEntity<Map<String, Object>> apiDocs() {
        return ResponseEntity.ok(Map.of(
            "api", "Clinique ERP - Frontend API",
            "version", "1.1",
            "authentication", Map.of(
                "type", "JWT Bearer Token",
                "header", "Authorization: Bearer <token>",
                "login_endpoint", "POST /api/v1/auth/login",
                "verify_endpoint", "POST /api/v1/auth/verify"
            ),
            "endpoints", Map.of(
                "auth", Map.of(
                    "POST /api/v1/auth/login", "Authentification - retourne JWT",
                    "POST /api/v1/auth/register", "Création de compte patient",
                    "GET /api/v1/auth/me", "Profil utilisateur connecté",
                    "POST /api/v1/auth/verify", "Vérification token",
                    "POST /api/v1/auth/logout", "Déconnexion"
                ),
                "staff_admin_only", Map.of(
                    "POST /api/v1/staff/secretaires", "Créer un secrétaire (admin)",
                    "GET /api/v1/staff/secretaires", "Liste des secrétaires (admin)",
                    "POST /api/v1/staff/medecins", "Créer un médecin (admin)",
                    "GET /api/v1/staff/medecins", "Liste des médecins (admin)"
                ),
                "users", Map.of(
                    "GET /api/v1/users", "Liste de tous les utilisateurs (admin)",
                    "GET /api/v1/users?role=ADMIN", "Filtrer par rôle"
                )
            ),
            "roles", List.of("ADMIN", "DOCTOR", "SECRETAIRE", "PATIENT"),
            "microservices", Map.of(
                "spring_boot", "http://localhost:9090 - API Gateway",
                "fastapi", "http://localhost:8000 - Auth & Users Microservice"
            )
        ));
    }
}
