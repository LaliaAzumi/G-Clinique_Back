package com.erp.clinique.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.model.Patient;
import com.erp.clinique.service.PatientService; // Assure-toi d'avoir un service

@RestController
@RequestMapping("/api/v1/patients")
public class PatientApiController {

    @Autowired
    private PatientService patientService;

    @GetMapping
    public ResponseEntity<List<Patient>> getAll() {
        // Cette méthode va chercher tous les patients via ton service
        List<Patient> patients = patientService.getAllPatients(); 
        return ResponseEntity.ok(patients);
    }
}