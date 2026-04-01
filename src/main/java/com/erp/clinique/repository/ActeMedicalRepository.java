package com.erp.clinique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.ActeMedical;

@Repository
public interface ActeMedicalRepository extends JpaRepository<ActeMedical, Long> {
    // Tu pourras ajouter des méthodes pour filtrer par catégorie plus tard
}
