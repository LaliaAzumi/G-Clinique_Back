package com.erp.clinique.repository;

import com.erp.clinique.model.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    List<Medecin> findByNomContainingIgnoreCase(String nom);

    List<Medecin> findBySpecialite(String specialite);
}
