package com.erp.clinique.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erp.clinique.model.Ordonnance;

public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {

}