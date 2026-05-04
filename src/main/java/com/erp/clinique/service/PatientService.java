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
            patientRepository.save(existingPatient); 
        } else {
            throw new RuntimeException("Patient introuvable avec l'id : " + patient.getId());
        }
    }
	public Patient update(Patient patient) {
        Patient existing = patientRepository.findById(patient.getId())
            .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        existing.setNom(patient.getNom());
        existing.setPrenom(patient.getPrenom());
        existing.setEmail(patient.getEmail());
        existing.setTelephone(patient.getTelephone());
        existing.setAdresse(patient.getAdresse());
        existing.setDateNaissance(patient.getDateNaissance());
        existing.setSexe(patient.getSexe());

        return patientRepository.save(existing);
    }

    public void delete(Long id) {
        Patient p = patientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        patientRepository.delete(p);
    }

    public void deletePatientById(Long id) {
        patientRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return patientRepository.existsByEmail(email);
    }
    public Patient getByEmail(String email) {
        return patientRepository.findByEmail(email);
    }
	

}
