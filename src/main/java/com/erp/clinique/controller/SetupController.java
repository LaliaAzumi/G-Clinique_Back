package com.erp.clinique.controller;

import com.erp.clinique.model.Users;
import com.erp.clinique.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur temporaire pour initialiser les données de démo
 * À supprimer en production
 */
@RestController
@RequestMapping("/api/setup")
public class SetupController {

    @Autowired
    private UserService userService;

    /**
     * Crée l'utilisateur démo s'il n'existe pas
     * Usage: POST /api/setup/demo-user
     */
    @PostMapping("/demo-user")
    public ResponseEntity<String> createDemoUser() {
        // Vérifie si l'utilisateur existe déjà
        if (userService.findByUsername("demo").isPresent()) {
            return ResponseEntity.ok("Utilisateur 'demo' existe déjà");
        }

        // Crée l'utilisateur démo - le mot de passe sera encodé par UserService
        Users demoUser = new Users();
        demoUser.setUsername("demo");
        demoUser.setEmail("demo@gmail.com");
        demoUser.setMdp("demo123"); // Mot de passe en clair, sera haché par UserService
        demoUser.setRole("ADMIN");
        demoUser.setFirstLogin(true);

        userService.saveUser(demoUser);

        return ResponseEntity.ok("Utilisateur démo créé avec succès!\n" +
                "Username: demo\n" +
                "Password: demo123\n" +
                "Email: demo@gmail.com");
    }
}
