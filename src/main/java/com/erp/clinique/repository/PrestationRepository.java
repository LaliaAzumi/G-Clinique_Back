package com.erp.clinique.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.Prestation;

@Repository
public interface PrestationRepository extends JpaRepository<Prestation, Long> {
    // Pour afficher la liste des actes d'un RDV (utile pour les résultats)
    List<Prestation> findByRendezvousId(Long rendezvousId);
    
    @Query("SELECT SUM(p.prixApplique) FROM Prestation p " +
    	       "WHERE p.rendezvous.medecin.id = :medecinId " +
    	       "AND p.rendezvous.statutPaiement = 'PAYE'")
    	Double calculerTotalBrut(@Param("medecinId") Long medecinId);
}
