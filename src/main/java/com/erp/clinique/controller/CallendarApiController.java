package com.erp.clinique.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.service.RendezVousService;
import com.erp.clinique.service.UserService;

@RestController
@RequestMapping("/api/v1/calendar")
public class CallendarApiController {

    @Autowired
    private RendezVousService rendezVousService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MedecinUserRepository medecinUserRepo;

    /**
     * Récupère les événements (RDV) pour un utilisateur spécifique (médecin)
     * URL : /api/v1/calendar/eventsN/5?startOfWeek=2026-04-06
     */
    @GetMapping("/eventsN/{userId}")
    public ResponseEntity<?> getEvents(
        @PathVariable Long userId,
        @RequestParam(required = false) String startOfWeek
    ) {
        try {
            // 1. On cherche l'utilisateur par son ID au lieu du Token
            Users user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur avec l'ID " + userId + " introuvable"));

            // 2. On récupère le lien MedecinUser
            MedecinUser medUser = medecinUserRepo.findByUserId(user.getId());
            if (medUser == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Cet utilisateur n'est pas rattaché à un profil médecin."));
            }

            // 3. Logique de dates
            Long medecinId = medUser.getMedecinId();
            LocalDate start = (startOfWeek != null) 
                    ? LocalDate.parse(startOfWeek) 
                    : LocalDate.now().with(DayOfWeek.MONDAY);
            LocalDate end = start.plusDays(6);

            // 4. Récupération des rendez-vous
            List<RendezVous> rdvs = rendezVousService.findByMedecinIdAndDateBetween(medecinId, start, end);
            System.out.println("DEBUG: Recherche RDV pour MedecinID: " + medecinId);
            System.out.println("DEBUG: Période du " + start + " au " + end);

            //List<RendezVous> rdvs = rendezVousService.findByMedecinIdAndDateBetween(medecinId, start, end);

            System.out.println("DEBUG: Nombre de RDV trouvés: " + rdvs.size());
            
            return ResponseEntity.ok(rdvs);

        } catch (Exception e) {
            // Gestion d'erreur propre pour éviter le crash du front
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération du calendrier : " + e.getMessage()));
        }
    }
}