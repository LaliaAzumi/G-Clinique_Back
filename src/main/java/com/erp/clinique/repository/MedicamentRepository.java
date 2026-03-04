package com.erp.clinique.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.Medicament;

@Repository
public interface MedicamentRepository extends JpaRepository<Medicament, Long>{
    List<Medicament> findByNomContainingIgnoreCase(String nom);

	
	@Query("""
	            SELECT m FROM Medicament m
	            WHERE 
	                LOWER(m.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
	                OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
	        """)
	        Page<Medicament> searchAll(@Param("keyword") String keyword, Pageable pageable);

	 List<Medicament> findByDescription(String description);
}
