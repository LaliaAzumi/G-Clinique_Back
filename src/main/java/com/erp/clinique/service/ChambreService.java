package com.erp.clinique.service;

import java.util.List; // N'oubliez pas l'import
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.clinique.model.Chambre;
import com.erp.clinique.repository.ChambreRepository;

@Service
public class ChambreService {

    @Autowired
    private ChambreRepository chambreRepository;

    // Ajoutez cette méthode !
    public List<Chambre> findAll() {
        return chambreRepository.findAll();
    }

    public Chambre save(Chambre chambre) {
        return chambreRepository.save(chambre);
    }

    public Optional<Chambre> findById(Long id) {
        return chambreRepository.findById(id);
    }

    public void deleteById(Long id) {
        chambreRepository.deleteById(id);
    }
}