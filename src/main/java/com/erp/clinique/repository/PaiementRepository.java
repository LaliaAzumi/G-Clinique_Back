package com.erp.clinique.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erp.clinique.model.Paiement;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    // Cette méthode permettra de retrouver le paiement lié à un RDV précis
    Optional<Paiement> findByRendezvousId(Long rendezvousId);
    
    // Pour ton futur système de lecture de SMS
    Optional<Paiement> findByCodeTransaction(String codeTransaction);
}