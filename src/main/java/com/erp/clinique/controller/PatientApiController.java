package com.erp.clinique.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erp.clinique.model.Patient;
import com.erp.clinique.service.PatientService; // Assure-toi d'avoir un service

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

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

    @PutMapping("/update")
    public ResponseEntity<Patient> updatePatient(@RequestBody Patient patient) {
        Patient updated = patientService.update(patient);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.ok("Patient supprimé avec succès");
    }

    @PostMapping("/save")
    public ResponseEntity<Patient> save(@RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.addPatient(patient));
    }
}