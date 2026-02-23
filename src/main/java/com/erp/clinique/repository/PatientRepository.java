package com.erp.clinique.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.erp.clinique.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

}
