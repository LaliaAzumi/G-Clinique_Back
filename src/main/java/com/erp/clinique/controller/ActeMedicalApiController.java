package com.erp.clinique.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.dto.ApiResponse;
import com.erp.clinique.model.ActeMedical;
import com.erp.clinique.repository.ActeMedicalRepository;

/**
 * API REST pour la gestion des actes médicaux (services de la clinique)
 */
@RestController
@RequestMapping("/api/v1/actes")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ActeMedicalApiController {

    @Autowired
    private ActeMedicalRepository acteRepo;

    /**
     * Liste tous les actes médicaux pour l'affichage public
     */
    @GetMapping
    public ResponseEntity<ApiResponse> listActes() {
        try {
            List<ActeMedical> actes = acteRepo.findAll();
            
            return ResponseEntity.ok(new ApiResponse(
                true, 
                "Liste des actes médicaux récupérée", 
                actes
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ApiResponse(false, "Erreur lors de la récupération : " + e.getMessage(), null));
        }
    }

    /**
     * Optionnel : Récupérer un acte spécifique par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getActe(@PathVariable Long id) {
        return acteRepo.findById(id)
            .map(acte -> ResponseEntity.ok(new ApiResponse(true, "Acte trouvé", acte)))
            .orElse(ResponseEntity.status(404).body(new ApiResponse(false, "Acte non trouvé", null)));
    }
}