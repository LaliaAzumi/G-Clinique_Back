package com.erp.clinique.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.RendezVous;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    List<RendezVous> findByMedecinId(Long medecinId);

    List<RendezVous> findByPatientId(Long patientId);

    List<RendezVous> findByStatut(String statut);
    
    List<RendezVous> findByMedecinIdAndDateBetween(Long medecinId, LocalDate start, LocalDate end);
}
