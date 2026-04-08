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
   
    public List<Medecin> findAll() {
        return medecinRepository.findAll();
    }

   
    public Optional<Medecin> findById(Long id) {
        return medecinRepository.findById(id);
    }

   
    public Medecin save(Medecin medecin) {
        return medecinRepository.save(medecin);
    }

   
    public void deleteById(Long id) {
        medecinRepository.deleteById(id);
    }

   
    public List<Medecin> searchByNom(String nom) {
        return medecinRepository.findByNomContainingIgnoreCase(nom);
    }

    public List<Medecin> findBySpecialite(String specialite) {
        return medecinRepository.findBySpecialite(specialite);
    }
    
    //return avec les medecinUser
    public List<Map<String, Object>> findAllEnriched() {
        // 1. On récupère toutes les relations (IDs)
        List<MedecinUser> relations = medecinUserRepository.findAll();

        return relations.stream().map(rel -> {
            Map<String, Object> medecinData = new HashMap<>();
            
            // 2. On ajoute les IDs de base
            medecinData.put("id", rel.getId());
            medecinData.put("medecinId", rel.getMedecinId());
            medecinData.put("userId", rel.getUserId());

            // 3. On va chercher le Nom et la Spécialité (Table Medecin)
            medecinRepository.findById(rel.getMedecinId()).ifPresent(m -> {
                medecinData.put("nom", m.getNom());
                medecinData.put("specialite", m.getSpecialite());
                medecinData.put("telephone", m.getTelephone());
                medecinData.put("adresse", m.getAdresse());
            });

            // 4. On va chercher l'Email et le Username (Table User)
            userRepository.findById(rel.getUserId()).ifPresent(u -> {
                medecinData.put("username", u.getUsername());
                medecinData.put("email", u.getEmail());
            });

            return medecinData;
        }).collect(Collectors.toList());
    }
}
