package com.erp.clinique.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.clinique.model.Medecin;
import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Patient;
import com.erp.clinique.repository.MedecinRepository;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.UserRepository;

@Service
@Transactional
public class MedecinService {

    @Autowired
    private MedecinRepository medecinRepository;
    @Autowired
    private MedecinUserRepository medecinUserRepository;
    @Autowired
    private UserRepository userRepository;
    
    
    public Long findUserIdByMedecinId(Long medecinId) {
        return medecinUserRepository.findByMedecinId(medecinId)
                .map(MedecinUser::getUserId)
                .orElse(null);
    }
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }
    public List<Medecin> findAll() {
        return medecinRepository.findAll();
    }

   
    public Optional<Medecin> findById(Long id) {
        return medecinRepository.findById(id);
    }

   
    public Medecin save(Medecin medecin) {
        return medecinRepository.save(medecin);
    }

   
    public boolean deleteById(Long id) {
        if (medecinRepository.existsById(id)) {
            medecinRepository.deleteById(id);
            return true;
        }
        return false;
    }

   
    public List<Medecin> searchByNom(String nom) {
        return medecinRepository.findByNomContainingIgnoreCase(nom);
    }

    public List<Medecin> findBySpecialite(String specialite) {
        return medecinRepository.findBySpecialite(specialite);
    }
    
    //return avec les medecinUser
    public List<Map<String, Object>> findAllEnriched() {
    // 1. On part de TOUS les médecins (on est sûr de ne personne oublier)
    List<Medecin> allMedecins = medecinRepository.findAll();

    return allMedecins.stream().map(m -> {
        Map<String, Object> medecinData = new HashMap<>();
        
        // 2. On ajoute les infos du médecin (toujours présentes)
        medecinData.put("id", m.getId()); // ID pour le frontend
        medecinData.put("medecinId", m.getId());
        medecinData.put("nom", m.getNom());
        medecinData.put("specialite", m.getSpecialite());
        medecinData.put("telephone", m.getTelephone());
        medecinData.put("adresse", m.getAdresse());

        // 3. On cherche s'il y a une relation utilisateur
        medecinUserRepository.findByMedecinId(m.getId()).ifPresent(rel -> {
            medecinData.put("userId", rel.getUserId());
            
            // 4. Si la relation existe, on cherche les infos dans la table User
            userRepository.findById(rel.getUserId()).ifPresent(u -> {
                medecinData.put("username", u.getUsername());
                medecinData.put("email", u.getEmail());
            });
        });

        // Si pas d'utilisateur, les champs userId, username et email seront absents ou null
        // mais le médecin apparaîtra quand même dans la liste !
        
        return medecinData;
    }).collect(Collectors.toList());
}
    
    //delete full medecin 
    
}
