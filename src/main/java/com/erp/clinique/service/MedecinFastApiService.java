package com.erp.clinique.service;

import com.erp.clinique.model.Medecin;
import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.MedecinRepository;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.utils.MdpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Service pour créer des médecins et leurs comptes utilisateurs via FastAPI
 * Intègre la création du médecin dans la base locale et du compte user via microservice
 */
@Service
public class MedecinFastApiService {

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private MedecinUserRepository medecinUserRepository;

    @Autowired
    private FastApiUserService fastApiUserService;

    @Autowired
    private EmailService emailService;

    /**
     * Crée un médecin et son compte utilisateur associé via FastAPI
     * @param medecin Les informations du médecin
     * @param username Nom d'utilisateur pour le compte
     * @param email Email pour le compte
     * @return Map avec medecin et user créés, ou null si erreur
     */
    @Transactional
    public Map<String, Object> createMedecinWithUser(Medecin medecin, String username, String email) {
        try {
            // 1. Sauvegarde le médecin dans la base locale
            Medecin savedMedecin = medecinRepository.save(medecin);

            // 2. Génère un mot de passe aléatoire
            String randomPassword = MdpUtils.generateRandomMdp();

            // 3. Crée le compte utilisateur via FastAPI
            Map<String, Object> userResult = fastApiUserService.createUser(
                username,
                email,
                randomPassword,
                "MEDECIN"
            );

            if (userResult == null) {
                // Rollback: supprime le médecin créé
                medecinRepository.delete(savedMedecin);
                return null;
            }

            // 4. Récupère l'ID de l'utilisateur créé
            Long userId = null;
            if (userResult.containsKey("id")) {
                userId = Long.valueOf(userResult.get("id").toString());
            }

            // 5. Crée le lien medecin-user
            if (userId != null) {
                MedecinUser link = new MedecinUser(savedMedecin.getId(), userId);
                medecinUserRepository.save(link);
            }

            // 6. Envoie l'email avec les identifiants
            emailService.sendPasswordEmail(email, username, randomPassword);

            return Map.of(
                "medecin", savedMedecin,
                "user", userResult,
                "password", randomPassword,
                "message", "Médecin et compte utilisateur créés avec succès"
            );

        } catch (Exception e) {
            System.err.println("Erreur lors de la création du médecin avec user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crée uniquement le compte utilisateur pour un médecin existant
     * @param medecinId ID du médecin existant
     * @param username Nom d'utilisateur
     * @param email Email
     * @return true si création réussie
     */
    @Transactional
    public boolean createUserForExistingMedecin(Long medecinId, String username, String email) {
        try {
            // Vérifie que le médecin existe
            Optional<Medecin> medecinOpt = medecinRepository.findById(medecinId);
            if (medecinOpt.isEmpty()) {
                return false;
            }

            // Génère un mot de passe
            String randomPassword = MdpUtils.generateRandomMdp();

            // Crée l'utilisateur via FastAPI
            Map<String, Object> userResult = fastApiUserService.createUser(
                username,
                email,
                randomPassword,
                "MEDECIN"
            );

            if (userResult == null) {
                return false;
            }

            // Crée le lien
            Long userId = null;
            if (userResult.containsKey("id")) {
                userId = Long.valueOf(userResult.get("id").toString());
            }

            if (userId != null) {
                MedecinUser link = new MedecinUser(medecinId, userId);
                medecinUserRepository.save(link);
            }

            // Envoie l'email
            emailService.sendPasswordEmail(email, username, randomPassword);

            return true;

        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            return false;
        }
    }
}
