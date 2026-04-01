package com.erp.clinique.repository;

// IMPORTANTS : Ces trois imports doivent provenir de org.springframework.data
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erp.clinique.model.Chambre;

public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    
    @Query("SELECT c FROM Chambre c WHERE c.numero LIKE %:kw%")
    Page<Chambre> searchAll(@Param("kw") String keyword, Pageable pageable);
}