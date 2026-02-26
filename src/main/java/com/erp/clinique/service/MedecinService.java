package com.erp.clinique.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.clinique.model.Medecin;
import com.erp.clinique.repository.MedecinRepository;

@Service
@Transactional
public class MedecinService {

    @Autowired
    private MedecinRepository medecinRepository;

    // Lister tous les medecins
    public List<Medecin> findAll() {
        return medecinRepository.findAll();
    }

    // Trouver un medecin par ID
    public Optional<Medecin> findById(Long id) {
        return medecinRepository.findById(id);
    }

    // Creer ou mettre a jour un medecin
    public Medecin save(Medecin medecin) {
        return medecinRepository.save(medecin);
    }

    // Supprimer un medecin par ID
    public void deleteById(Long id) {
        medecinRepository.deleteById(id);
    }

    // Rechercher par nom
    public List<Medecin> searchByNom(String nom) {
        return medecinRepository.findByNomContainingIgnoreCase(nom);
    }

    // Rechercher par specialite
    public List<Medecin> findBySpecialite(String specialite) {
        return medecinRepository.findBySpecialite(specialite);
    }
}
