package com.erp.clinique.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erp.clinique.model.RendezVous;
import com.erp.clinique.repository.RendezVousRepository;

@Service
@Transactional
public class RendezVousService {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    // Lister tous les rendez-vous
    public List<RendezVous> findAll() {
        return rendezVousRepository.findAll();
    }

    // Trouver un rendez-vous par ID
    public Optional<RendezVous> findById(Long id) {
        return rendezVousRepository.findById(id);
    }

    // Creer ou mettre a jour un rendez-vous
    public RendezVous save(RendezVous rendezVous) {
        return rendezVousRepository.save(rendezVous);
    }

    // Supprimer un rendez-vous par ID
    public void deleteById(Long id) {
        rendezVousRepository.deleteById(id);
    }

    // Trouver les rendez-vous d'un medecin
    public List<RendezVous> findByMedecinId(Long medecinId) {
        return rendezVousRepository.findByMedecinId(medecinId);
    }

    // Trouver les rendez-vous d'un patient
    public List<RendezVous> findByPatientId(Long patientId) {
        return rendezVousRepository.findByPatientId(patientId);
    }

    // Trouver par statut
    public List<RendezVous> findByStatut(String statut) {
        return rendezVousRepository.findByStatut(statut);
    }
    
 // find 
    public List<RendezVous> findByMedecinIdAndDateBetween(Long medecinId, LocalDate start, LocalDate end) {
        return rendezVousRepository.findByMedecinIdAndDateBetween(medecinId, start, end);
    }
}
