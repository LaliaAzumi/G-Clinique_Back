package com.erp.clinique.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.Medecin;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    List<Medecin> findByNomContainingIgnoreCase(String nom);
    

    @Query("""
            SELECT m FROM Medecin m
            WHERE 
                LOWER(m.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.specialite) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.telephone) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.adresse) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
        Page<Medecin> searchAll(@Param("keyword") String keyword, Pageable pageable);
    List<Medecin> findBySpecialite(String specialite);
}
