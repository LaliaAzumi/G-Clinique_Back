package com.erp.clinique.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.model.Patient;
import com.erp.clinique.service.PatientService; // Assure-toi d'avoir un service

@RestController
@RequestMapping("/api/v1/patients")
@CrossOrigin("*") // Autorise les appels
public class PatientApiController {

    @Autowired
    private PatientService patientService;

    @GetMapping
    public ResponseEntity<List<Patient>> getAll(
        @RequestParam(required = false) Integer page, 
        @RequestParam(required = false) Integer size) {
        
        // Si vous n'avez pas encore de pagination en Java, 
        // ignorez juste les paramètres pour l'instant
        List<Patient> patients = patientService.getAllPatients(); 
        return ResponseEntity.ok(patients);
    }
}