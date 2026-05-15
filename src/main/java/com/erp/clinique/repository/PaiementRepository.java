package com.erp.clinique.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.Paiement;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    // Cette méthode permettra de retrouver le paiement lié à un RDV précis
    Optional<Paiement> findByRendezvousId(Long rendezvousId);
    
    // Pour ton futur système de lecture de SMS
    Optional<Paiement> findByCodeTransaction(String codeTransaction);

    @Query("SELECT SUM(p.montantEnvoye) FROM Paiement p")
    Double totalPaiement();

    // @Query("""
    //     SELECT DATE(p.datePaiement), SUM(p.montant)
    //     FROM Paiement p
    //     GROUP BY DATE(p.datePaiement)
    //     ORDER BY DATE(p.datePaiement)
    // """)
    // List<Object[]> paiementParJour();
}