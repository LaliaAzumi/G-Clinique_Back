package com.erp.clinique.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.erp.clinique.model.Patient;
import com.erp.clinique.service.PatientService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1/patients")
@CrossOrigin("*") 
public class PatientApiController {

    @Autowired
    private PatientService patientService;

    // 1. LIRE TOUS LES PATIENTS
    @GetMapping
    public ResponseEntity<List<Patient>> getAll() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    // 2. CRÉER UN PATIENT (C'est ce qui manquait pour l'ajout !)
    @PostMapping("/save")
    public ResponseEntity<Patient> save(@RequestBody Patient patient) {
        // Le @RequestBody est CRUCIAL pour lire le JSON envoyé par FastAPI
        Patient savedPatient = patientService.savePatient(patient);
        return ResponseEntity.ok(savedPatient);
    }


    @PutMapping("/update")
    public ResponseEntity<Patient> update(@RequestBody Patient patient) {
        // Spring Data JPA fera un "update" au lieu d'un "insert" car l'ID est présent
        Patient updatedPatient = patientService.savePatient(patient);
        return ResponseEntity.ok(updatedPatient);
    }

    // 4. SUPPRIMER UN PATIENT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok().build();
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