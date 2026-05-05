package com.erp.clinique.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

import com.erp.clinique.model.Consultation;
import com.erp.clinique.repository.ConsultationRepository;

@Service
@Transactional
public class ConsultationService {

    @Autowired
    private ConsultationRepository consultationRepository;

   
    public List<Consultation> findAll() {
        return consultationRepository.findAll();
    }

    public Optional<Consultation> findById(Long id) {
        return consultationRepository.findById(id);
    }

    
    public Consultation save(Consultation consultation) {
        return consultationRepository.save(consultation);
    }

    
    public void deleteById(Long id) {
        consultationRepository.deleteById(id);
    }

    public Optional<Consultation> findByRendezVousId(Long rendezVousId) {
        return consultationRepository.findByRendezVousId(rendezVousId);
    }

	public List<Object[]> countConsultationsByMonth() {
		return consultationRepository.countConsultationsByMonth();
	}
    public Page<Consultation> findByMedecinId(Long medecinId, Pageable pageable) {
        return consultationRepository.findByRendezVousMedecinId(medecinId, pageable);
    }
}
