package com.erp.clinique.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service pour créer des utilisateurs via le microservice FastAPI
 * Déplace la logique de création d'utilisateurs vers FastAPI
 */
@Service
public class FastApiUserService {

    @Value("${fastapi.url:http://localhost:8000}")
    private String fastApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Crée un utilisateur via FastAPI
     * @param username Nom d'utilisateur
     * @param email Email
     * @param password Mot de passe
     * @param role Rôle (ADMIN, DOCTOR, SECRETARY, PATIENT)
     * @return Map contenant les informations de l'utilisateur créé
     */
    public Map<String, Object> createUser(String username, String email, String password, String role) {
        try {
            Map<String, String> userData = Map.of(
                "username", username,
                "email", email,
                "password", password,
                "role", role
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(userData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                fastApiUrl + "/api/users/create",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'utilisateur: " + e.getMessage());
            return null;
        }
    }

    /**
     * Liste tous les utilisateurs via FastAPI
     * @param role Filtre optionnel par rôle
     * @return Map contenant la liste des utilisateurs
     */
    public Map<String, Object> listUsers(String role) {
        try {
            String url = fastApiUrl + "/api/users/list";
            if (role != null && !role.isEmpty()) {
                url += "?role=" + role;
            }

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
            return null;
        }
    }

    /**
     * Supprime un utilisateur via FastAPI
     * @param userId ID de l'utilisateur à supprimer
     * @return true si suppression réussie
     */
    public boolean deleteUser(Long userId) {
        try {
            String url = fastApiUrl + "/api/users/" + userId;
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
            return false;
        }
    }

    /**
     * Met à jour un utilisateur via FastAPI
     * @param userId ID de l'utilisateur
     * @param username Nouveau nom d'utilisateur
     * @param email Nouvel email
     * @param role Nouveau rôle
     * @return Map contenant les informations de l'utilisateur mis à jour
     */
    public Map<String, Object> updateUser(Long userId, String username, String email, String role) {
        try {
            Map<String, String> userData = Map.of(
                "username", username,
                "email", email,
                "role", role
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(userData, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                fastApiUrl + "/api/users/" + userId,
                HttpMethod.PUT,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
            return null;
        }
    }
}
