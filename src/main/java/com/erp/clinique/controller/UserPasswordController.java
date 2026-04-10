package com.erp.clinique.controller;

import com.erp.clinique.model.Users;
import com.erp.clinique.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserPasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (username == null || newPassword == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Paramètres manquants"));
        }

        Users user = userService.findByUsername(username)
            .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404)
                .body(Map.of("success", false, "message", "Utilisateur non trouvé"));
        }

        // Vérifier le mot de passe actuel si ce n'est pas la première connexion
        if (!user.isFirstLogin() && currentPassword != null) {
            if (!passwordEncoder.matches(currentPassword, user.getMdp())) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Mot de passe actuel incorrect"));
            }
        }

        // Mettre à jour le mot de passe
        user.setMdp(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false); // Marquer comme connecté
        userService.saveUser(user);

        return ResponseEntity.ok()
            .body(Map.of("success", true, "message", "Mot de passe modifié avec succès"));
    }

    @GetMapping("/{username}/first-login")
    public ResponseEntity<?> checkFirstLogin(@PathVariable String username) {
        Users user = userService.findByUsername(username)
            .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404)
                .body(Map.of("success", false, "message", "Utilisateur non trouvé"));
        }

        return ResponseEntity.ok()
            .body(Map.of("success", true, "firstLogin", user.isFirstLogin()));
    }
}
