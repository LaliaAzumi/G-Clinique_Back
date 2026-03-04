package com.erp.clinique.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.RendezVous;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    List<RendezVous> findByMedecinId(Long medecinId);

    List<RendezVous> findByPatientId(Long patientId);

    List<RendezVous> findByStatut(String statut);
    
    List<RendezVous> findByMedecinIdAndDateBetween(Long medecinId, LocalDate start, LocalDate end);
    
 // ✅ Trouver tous les RDV d’un médecin à une date précise
    List<RendezVous> findByMedecinIdAndDate(Long medecinId, LocalDate date);
    

    @Query("""
            SELECT r FROM RendezVous r
    		WHERE 
    		
		        LOWER(r.motif) LIKE LOWER(CONCAT('%', :keyword, '%'))
		        OR LOWER(r.statut) LIKE LOWER(CONCAT('%', :keyword, '%'))
		        OR LOWER(r.medecin.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
		        OR LOWER(r.medecin.specialite) LIKE LOWER(CONCAT('%', :keyword, '%'))
		        OR LOWER(r.patient.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
		        OR LOWER(r.patient.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))
		         OR CAST(r.date AS string) LIKE CONCAT('%', :keyword, '%')
        """)
        Page<RendezVous> searchAll(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT r FROM RendezVous r WHERE " +
    	       "(:keyword IS NULL OR LOWER(r.motif) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
    	       "OR LOWER(r.patient.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
    	       "OR LOWER(r.medecin.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
    	       "AND (:date IS NULL OR r.date = :date) " +
    	       "AND (:statut IS NULL OR r.statut = :statut)")
    	List<RendezVous> searchRendezVous(@Param("keyword") String keyword, 
    	                                  @Param("date") LocalDate date, 
    	                                  @Param("statut") String statut);
    
}
