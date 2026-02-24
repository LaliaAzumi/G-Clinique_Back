package com.erp.clinique.service;

import com.erp.clinique.model.Consultation;
import com.erp.clinique.repository.ConsultationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConsultationService {

    @Autowired
    private ConsultationRepository consultationRepository;

    // Lister toutes les consultations
    public List<Consultation> findAll() {
        return consultationRepository.findAll();
    }

    // Trouver une consultation par ID
    public Optional<Consultation> findById(Long id) {
        return consultationRepository.findById(id);
    }

    // Creer ou mettre a jour une consultation
    public Consultation save(Consultation consultation) {
        return consultationRepository.save(consultation);
    }

    // Supprimer une consultation par ID
    public void deleteById(Long id) {
        consultationRepository.deleteById(id);
    }

    // Trouver la consultation d'un rendez-vous
    public Optional<Consultation> findByRendezVousId(Long rendezVousId) {
        return consultationRepository.findByRendezVousId(rendezVousId);
    }
}
