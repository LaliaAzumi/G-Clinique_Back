package com.erp.clinique.repository;

import com.erp.clinique.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("""
        SELECT SUM(p.quantite * m.pu)
        FROM Prescription p
        JOIN p.medicament m
    """)
    Double totalMedicaments();
}