package com.erp.clinique.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erp.clinique.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
	boolean existsByEmail(String email);
	Patient findByEmail(String email);
	@Query("""
            SELECT p FROM Patient p
            WHERE 
                LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))
               
                OR LOWER(p.telephone) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.adresse) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR CAST(p.dateNaissance AS string) LIKE CONCAT('%', :keyword, '%')

        """)
        Page<Patient> searchAll(@Param("keyword") String keyword, Pageable pageable);
    

}
