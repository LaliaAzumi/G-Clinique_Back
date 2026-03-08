package com.erp.clinique.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.Consultation;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    Optional<Consultation> findByRendezVousId(Long rendezVousId);
    
    @Query("""
            SELECT c FROM Consultation c
            WHERE 
                LOWER(c.diagnostique) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.rendezVous.patient.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.rendezVous.patient.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR CAST(c.date AS string) LIKE CONCAT('%', :keyword, '%')

        """)
        Page<Consultation> searchAll(@Param("keyword") String keyword, Pageable pageable);
    

    @Query("SELECT MONTH(c.date), COUNT(c) FROM Consultation c WHERE YEAR(c.date) = YEAR(CURRENT_DATE) GROUP BY MONTH(c.date)")
    List<Object[]> countConsultationsByMonth();


    Page<Consultation> findByRendezVousMedecinId(Long medecinId, Pageable pageable);

  
    Page<Consultation> findByRendezVousMedecinIdAndRendezVousPatientNomContainingIgnoreCase(
            Long medecinId, String nom, Pageable pageable);
    
    Page<Consultation> findByRendezVousMedecinIdAndDate(Long medecinId, LocalDate date, Pageable pageable);

    Page<Consultation> findByRendezVousMedecinIdAndRendezVousPatientNomContainingIgnoreCaseAndDate(
        Long medecinId, String keyword, LocalDate date, Pageable pageable);

}
