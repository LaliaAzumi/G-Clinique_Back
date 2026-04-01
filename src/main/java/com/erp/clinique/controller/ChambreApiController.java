package com.erp.clinique.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.dto.ApiResponse;
import com.erp.clinique.model.Chambre;
import com.erp.clinique.service.ChambreService;
import com.erp.clinique.service.FastApiAuthService;

/**
 * API REST pour la gestion des chambres
 */
@RestController
@RequestMapping("/api/v1/chambres")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChambreApiController {

    @Autowired
    private ChambreService chambreService;

    @Autowired
    private FastApiAuthService fastApiAuthService;

    /**
     * Liste toutes les chambres
     */
    @GetMapping
    public ResponseEntity<ApiResponse> listChambres() {
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Liste des chambres récupérée avec succès",
            Map.of("chambres", chambreService.findAll()) // Assurez-vous d'avoir findAll() dans votre Service
        ));
    }

    /**
     * Récupère une chambre par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getChambre(@PathVariable Long id) {
        return chambreService.findById(id)
            .map(chambre -> ResponseEntity.ok(new ApiResponse(
                true,
                "Chambre trouvée",
                Map.of("chambre", chambre)
            )))
            .orElseGet(() -> ResponseEntity.status(404)
                .body(new ApiResponse(false, "Chambre non trouvée", null)));
    }

    /**
     * Crée une nouvelle chambre (Accès ADMIN uniquement)
     */
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createChambre(
            @RequestBody Chambre chambre,
            @RequestHeader("Authorization") String authHeader) {

        // Vérification de sécurité (Token + Rôle)
        ResponseEntity<ApiResponse> authError = checkAdminAuth(authHeader);
        if (authError != null) return authError;

        try {
            Chambre nouvelleChambre = chambreService.save(chambre);
            return ResponseEntity.ok(new ApiResponse(
                true, 
                "Chambre créée avec succès", 
                Map.of("chambre", nouvelleChambre)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur lors de la création : " + e.getMessage(), null));
        }
    }

    /**
     * Supprime une chambre (Accès ADMIN uniquement)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteChambre(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        ResponseEntity<ApiResponse> authError = checkAdminAuth(authHeader);
        if (authError != null) return authError;

        try {
            chambreService.deleteById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Chambre supprimée", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Impossible de supprimer la chambre (elle est peut-être liée à une hospitalisation)", null));
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
    
    /**
     * Modifie une chambre (Accès ADMIN uniquement)
     */
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateChambre(
            @PathVariable Long id,
            @RequestBody Chambre chambreDetails,
            @RequestHeader("Authorization") String authHeader) {

        // 1. Vérification de sécurité
        ResponseEntity<ApiResponse> authError = checkAdminAuth(authHeader);
        if (authError != null) return authError;

        try {
            return chambreService.findById(id).map(chambre -> {
                // 2. Mise à jour des champs (assurez-vous que les setters existent)
                chambre.setNumero(chambreDetails.getNumero());
                chambre.setEtat(chambreDetails.isEtat());
                chambre.setPrixJ(chambreDetails.getPrixJ());
                chambre.setEtage(chambreDetails.getEtage());
                chambre.setSoinsIntensifs(chambreDetails.isSoinsIntensifs());
                
                // 3. Sauvegarde
                Chambre updated = chambreService.save(chambre);
                return ResponseEntity.ok(new ApiResponse(true, "Chambre mise à jour avec succès", Map.of("chambre", updated)));
            }).orElseGet(() -> ResponseEntity.status(404)
                .body(new ApiResponse(false, "Chambre non trouvée avec l'ID : " + id, null)));
                
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur lors de la modification : " + e.getMessage(), null));
        }
    }
}
