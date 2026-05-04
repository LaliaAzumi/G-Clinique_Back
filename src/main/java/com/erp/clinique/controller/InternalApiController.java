package com.erp.clinique.controller;

import com.erp.clinique.dto.ApiResponse;
import com.erp.clinique.model.Users;
import com.erp.clinique.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API interne pour les communications Spring Boot <-> FastAPI
 * Ces endpoints sont appeles par FastAPI pour gerer les utilisateurs
 * FastAPI ne touche jamais directement la base de donnees
 */
@RestController
@RequestMapping("/api/internal")
public class InternalApiController {

    @Autowired
    private UserService userService;

    /**
     * Crée un utilisateur (appele par FastAPI)
     * FastAPI hache le mot de passe, Spring Boot sauvegarde en DB
     */
    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> userData) {
        try {
            String username = (String) userData.get("username");
            String email = (String) userData.get("email");
            String password = (String) userData.get("password"); // Deja hache par FastAPI
            String role = (String) userData.get("role");
            Boolean firstLogin = (Boolean) userData.getOrDefault("firstLogin", true);

            // Verifie si l'utilisateur existe deja
            if (userService.findByUsername(username).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username already exists"));
            }

            // Cree l'utilisateur
            Users user = new Users();
            user.setUsername(username);
            user.setEmail(email);
            user.setMdp(password); // Mot de passe deja hache par FastAPI
            user.setRole(role);
            user.setFirstLogin(firstLogin);

            Users savedUser = userService.saveUserInternal(user);

            return ResponseEntity.ok(Map.of(
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail(),
                "role", savedUser.getRole(),
                "message", "User created successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verifie si un utilisateur existe (appele par FastAPI)
     */
    @GetMapping("/users/exists")
    public ResponseEntity<Map<String, Boolean>> checkUserExists(
            @RequestParam String username,
            @RequestParam String email) {
        
        boolean usernameExists = userService.findByUsername(username).isPresent();
        // Note: vous devriez aussi verifier l'email via UserRepository
        
        return ResponseEntity.ok(Map.of(
            "exists", usernameExists,
            "usernameExists", usernameExists
        ));
    }

    /**
     * Recupere un utilisateur par username (appele par FastAPI)
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<Users> user = userService.findByUsername(username);
        
        if (user.isPresent()) {
            Users u = user.get();
            return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "email", u.getEmail(),
                "role", u.getRole(),
                "firstLogin", u.isFirstLogin()
            ));
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "User not found"));
    }

    /**
     * Liste les utilisateurs (appele par FastAPI)
     */
    @GetMapping("/users/list")
    public ResponseEntity<Map<String, Object>> listUsers(
            @RequestParam(required = false) String role) {
        
        List<Users> users;
        if (role != null && !role.isEmpty()) {
            users = userService.findByRole(role);
        } else {
            users = userService.findAll();
        }

        List<Map<String, Object>> userList = new java.util.ArrayList<>();
        for (Users u : users) {
            Map<String, Object> userMap = new java.util.HashMap<>();
            userMap.put("id", u.getId());
            userMap.put("username", u.getUsername());
            userMap.put("email", u.getEmail());
            userMap.put("role", u.getRole());
            userMap.put("firstLogin", u.isFirstLogin());
            userList.add(userMap);
        }

        return ResponseEntity.ok(Map.of(
            "users", userList,
            "count", userList.size()
        ));
    }

    // update user (appele par FastAPI)
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> userData) {

        try {
            Users user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (userData.containsKey("username")) {
                user.setUsername((String) userData.get("username"));
            }

            if (userData.containsKey("email")) {
                user.setEmail((String) userData.get("email"));
            }

            if (userData.containsKey("role")) {
                user.setRole((String) userData.get("role"));
            }

            Users updated = userService.saveUserInternal(user);

            return ResponseEntity.ok(Map.of(
                    "id", updated.getId(),
                    "username", updated.getUsername(),
                    "email", updated.getEmail(),
                    "role", updated.getRole(),
                    "message", "User updated successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprime un utilisateur (appele par FastAPI)
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteById(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
