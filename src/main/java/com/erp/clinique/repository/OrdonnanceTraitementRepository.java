package com.erp.clinique.repository;

import com.erp.clinique.model.OrdonnanceTraitement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdonnanceTraitementRepository extends JpaRepository<OrdonnanceTraitement, Long> {
    List<OrdonnanceTraitement> findByOrdonnanceId(Long ordonnanceId);
}
