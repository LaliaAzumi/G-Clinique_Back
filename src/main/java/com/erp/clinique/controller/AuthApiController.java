package com.erp.clinique.controller;

import com.erp.clinique.dto.AuthValidationRequest;
import com.erp.clinique.dto.AuthValidationResponse;
import com.erp.clinique.dto.UserInfoDTO;
import com.erp.clinique.model.Users;
import com.erp.clinique.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Contrôleur REST pour l'API d'authentification
 * Expose les endpoints utilisés par le microservice FastAPI
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Autorise les requêtes depuis FastAPI
public class AuthApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint de validation des identifiants
     * Appelé par FastAPI pour vérifier username/password
     */
    @PostMapping("/auth/validate")
    public ResponseEntity<AuthValidationResponse> validateCredentials(
            @RequestBody AuthValidationRequest request) {
        
        // Recherche l'utilisateur par username
        Optional<Users> userOpt = userService.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(new AuthValidationResponse(
                false, 
                "Utilisateur non trouvé"
            ));
        }
        
        Users user = userOpt.get();
        
        // Vérifie le mot de passe
        boolean passwordMatches = passwordEncoder.matches(
            request.getPassword(), 
            user.getMdp()
        );
        
        if (!passwordMatches) {
            return ResponseEntity.ok(new AuthValidationResponse(
                false, 
                "Mot de passe incorrect"
            ));
        }
        
        // Authentification réussie
        return ResponseEntity.ok(new AuthValidationResponse(
            true,
            "Authentification réussie",
            user.getId(),
            user.getUsername(),
            user.getRole()
        ));
    }

    /**
     * Endpoint pour récupérer les informations d'un utilisateur
     * Appelé par FastAPI après validation
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<UserInfoDTO> getUserByUsername(@PathVariable String username) {
        Optional<Users> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Users user = userOpt.get();
        UserInfoDTO userInfo = new UserInfoDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole()
        );
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Endpoint de vérification de santé
     * Permet à FastAPI de vérifier que Spring Boot est disponible
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Spring Boot is running");
    }
}
