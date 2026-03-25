package com.erp.clinique.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service pour valider les tokens JWT auprès du microservice FastAPI
 */
@Service
public class FastApiAuthService {

    @Value("${fastapi.url:http://localhost:8000}")
    private String fastApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Valide un token JWT en appelant FastAPI
     * @param token Le token JWT à valider
     * @return Les claims du token si valide, null sinon
     */
    public Map<String, Object> validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                fastApiUrl + "/api/auth/verify",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("valid"))) {
                    return body;
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erreur lors de la validation du token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Authentifie un utilisateur via FastAPI et retourne le token
     * @param username Nom d'utilisateur
     * @param password Mot de passe
     * @return Le token JWT si authentification réussie, null sinon
     */
    public String authenticate(String username, String password) {
        try {
            Map<String, String> credentials = Map.of(
                "username", username,
                "password", password
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(credentials, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                fastApiUrl + "/api/auth/login",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    Map<String, Object> tokenData = (Map<String, Object>) body.get("token");
                    return (String) tokenData.get("access_token");
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'authentification: " + e.getMessage());
            return null;
        }
    }
}
