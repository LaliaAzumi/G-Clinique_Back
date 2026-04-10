package com.erp.clinique.repository;

import com.erp.clinique.model.CategorieTraitement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategorieTraitementRepository extends JpaRepository<CategorieTraitement, Long> {
    Optional<CategorieTraitement> findByCode(String code);
}
