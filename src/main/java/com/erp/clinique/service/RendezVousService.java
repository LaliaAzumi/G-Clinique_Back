package com.erp.clinique.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<RendezVous> findAll() {
        return rendezVousRepository.findAll();
    }

    public Optional<RendezVous> findById(Long id) {
        return rendezVousRepository.findById(id);
    }

    public RendezVous save(RendezVous rendezVous) {
        return rendezVousRepository.save(rendezVous);
    }

    public void deleteById(Long id) {
        rendezVousRepository.deleteById(id);
    }

    public List<RendezVous> findByMedecinId(Long medecinId) {
        return rendezVousRepository.findByMedecinId(medecinId);
    }

    public List<RendezVous> findByPatientId(Long patientId) {
        return rendezVousRepository.findByPatientId(patientId);
    }

    public List<RendezVous> findByStatut(String statut) {
        return rendezVousRepository.findByStatut(statut);
    }
    
    public List<RendezVous> findByMedecinIdAndDateBetween(Long medecinId, LocalDate start, LocalDate end) {
        return rendezVousRepository.findByMedecinIdAndDateBetween(medecinId, start, end);
    }
    
    public List<LocalTime> getHeuresDisponibles(Long medecinId, LocalDate date) {
        List<RendezVous> rdvs = rendezVousRepository.findByMedecinIdAndDate(medecinId, date)
            .stream()
            .filter(r -> !r.getStatut().equals("ANNULE")) 
            .collect(Collectors.toList());

        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(18, 0);
        Duration dureeRdv = Duration.ofMinutes(60);

        List<LocalTime> heuresDisponibles = new ArrayList<>();
        for (LocalTime t = start; t.isBefore(end); t = t.plus(dureeRdv)) {
            final LocalTime horaire = t; 
            boolean libre = rdvs.stream().noneMatch(r -> r.getHeure().equals(horaire));
            if (libre) heuresDisponibles.add(horaire);
        }

        return heuresDisponibles;
    }
    public List<RendezVous> search(String keyword, LocalDate date, String statut) {
       
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null; 
        String searchStatut = (statut != null && !statut.trim().isEmpty()) ? statut : null;

        return rendezVousRepository.searchRendezVous(searchKeyword, date, searchStatut);
    }
}
