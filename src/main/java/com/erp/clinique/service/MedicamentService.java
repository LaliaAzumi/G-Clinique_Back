package com.erp.clinique.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.clinique.model.Medicament;
import com.erp.clinique.repository.MedicamentRepository;

@Service
@Transactional
public class MedicamentService {
    @Autowired
    private MedicamentRepository medicamentRepository;

    // Lister tous les medicaments
    public List<Medicament> findAll() {
        return medicamentRepository.findAll();
    }

    // Trouver un medicament par ID
    public Optional<Medicament> findById(Long id) {
        return medicamentRepository.findById(id);
    }

    // Creer ou mettre a jour un medicament
    public Medicament save(Medicament medicament) {
        return medicamentRepository.save(medicament);
    }

    // Supprimer un medicament par ID
    public void deleteById(Long id) {
        medicamentRepository.deleteById(id);
    }

    // Rechercher par nom
    public List<Medicament> searchByNom(String nom) {
        return medicamentRepository.findByNomContainingIgnoreCase(nom);
    }

    // Rechercher par specialite
    public List<Medicament> findByDescription(String description) {
        return medicamentRepository.findByDescription(description);
    }
}
