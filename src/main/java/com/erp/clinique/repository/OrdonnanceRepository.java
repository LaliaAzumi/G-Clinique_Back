package com.erp.clinique.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}