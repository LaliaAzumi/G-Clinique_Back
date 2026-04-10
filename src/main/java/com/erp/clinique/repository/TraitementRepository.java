package com.erp.clinique.repository;

import com.erp.clinique.model.CategorieTraitement;
import com.erp.clinique.model.Traitement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraitementRepository extends JpaRepository<Traitement, Long> {
    List<Traitement> findByCategorie(CategorieTraitement categorie);
    List<Traitement> findByCategorieId(Long categorieId);
    List<Traitement> findByType(String type); // MEDICAMENT ou ACTE_MEDICAL
    List<Traitement> findByNomContainingIgnoreCase(String nom);
}
