package com.erp.clinique.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.clinique.model.Patient;
import com.erp.clinique.repository.PatientRepository;

@Service
public class PatientService {
	
	@Autowired
	private PatientRepository patientRepository;

	public Patient addPatient(Patient patient) {
		return patientRepository.save(patient);
	}
	
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
  
    
    // Trouver un patient par ID
    public Optional<Patient> getPatientById(Long id) {
        return patientRepository.findById(id);
    }

    public void updatePatient(Patient patient) {
        Optional<Patient> existingPatientOpt = patientRepository.findById(patient.getId());
        if (existingPatientOpt.isPresent()) {
            Patient existingPatient = existingPatientOpt.get();
            existingPatient.setNom(patient.getNom());
            existingPatient.setPrenom(patient.getPrenom());
            existingPatient.setDateNaissance(patient.getDateNaissance());
            existingPatient.setTelephone(patient.getTelephone());
            existingPatient.setAdresse(patient.getAdresse());
            patientRepository.save(existingPatient); // JPA fera un UPDATE
        } else {
            throw new RuntimeException("Patient introuvable avec l'id : " + patient.getId());
        }
    }
	
    public void deletePatientById(Long id) {
        patientRepository.deleteById(id);
    }

	
	
	

}
