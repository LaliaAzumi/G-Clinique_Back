package com.erp.clinique.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erp.clinique.dto.RevenuMensuelDTO;
import com.erp.clinique.model.Ordonnance;

public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {
	@Query("""
		    SELECT o FROM Ordonnance o 
		    JOIN o.consultation c 
		    JOIN c.rendezVous r 
		    JOIN r.patient p 
		    WHERE LOWER(p.nom) LIKE LOWER(CONCAT('%', :patientName, '%'))
		       OR LOWER(p.prenom) LIKE LOWER(CONCAT('%', :patientName, '%'))
		""")
			List<Ordonnance> findByPatientName(@Param("patientName") String patientName);

	
	//ici c'est pou le calcul du revenu mensuel
	@Query("SELECT new com.erp.clinique.dto.RevenuMensuelDTO(MONTH(o.consultation.date), SUM(p.quantite * m.pu)) " +
           "FROM Ordonnance o " +
           "JOIN o.prescriptions p " +
           "JOIN p.medicament m " +
           "WHERE o.paye = true " +
           "AND YEAR(o.consultation.date) = :year " +
           "GROUP BY MONTH(o.consultation.date) " +
           "ORDER BY MONTH(o.consultation.date) ASC")
    List<RevenuMensuelDTO> findMonthlyRevenueByYear(@Param("year") int year);
	//pour avoir les sommes de medicament, chambre séparément
	@Query("SELECT SUM(p.quantite * m.pu) FROM Ordonnance o " +
       "JOIN o.prescriptions p JOIN p.medicament m " +
       "WHERE o.paye = true AND o.consultation.date BETWEEN :debut AND :fin")
	Double sumMedicamentsByDate(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

	@Query("SELECT COUNT(c) FROM Consultation c WHERE c.date BETWEEN :debut AND :fin")
	Long countConsultationsByDate(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
}