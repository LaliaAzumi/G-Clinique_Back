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

 
    public List<Medicament> findAll() {
        return medicamentRepository.findAll();
    }

    public Optional<Medicament> findById(Long id) {
        return medicamentRepository.findById(id);
    }

    
    public Medicament save(Medicament medicament) {
        return medicamentRepository.save(medicament);
    }

    public void deleteById(Long id) {
        medicamentRepository.deleteById(id);
    }

    public List<Medicament> searchByNom(String nom) {
        return medicamentRepository.findByNomContainingIgnoreCase(nom);
    }

    public List<Medicament> findByDescription(String description) {
        return medicamentRepository.findByDescription(description);
    }
}
