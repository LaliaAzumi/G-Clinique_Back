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
}
