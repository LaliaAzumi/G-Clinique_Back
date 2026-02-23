package com.erp.clinique.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.erp.clinique.exception.EntityNotFoundException;
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
  
    public Patient getPatientById(Long id) {
		 Optional<Patient> existingPatient = patientRepository.findById(id);

		    if (existingPatient.isPresent()) {
		        
		        return existingPatient.get();
		    } else {
		        throw new EntityNotFoundException("Patient non trouvé avec id " + id);
		    }
   }

    public Patient updatePatient(Long id, Patient patient) {
		 Optional<Patient> existingPatient = patientRepository.findById(id);

		    if (existingPatient.isPresent()) {
		        Patient patientToUpdate = existingPatient.get();
		        patientToUpdate.setNom(patient.getNom());
		        patientToUpdate.setPrenom(patient.getPrenom());
		        patientToUpdate.setDateNaissance(patient.getDateNaissance());
		        patientToUpdate.setTelephone(patient.getTelephone());
		        patientToUpdate.setAdresse(patient.getAdresse());
		        return patientRepository.save(patientToUpdate);
		    } else {
		        throw new EntityNotFoundException("Patient non trouvé avec id " + id);
		    }
    }
	
    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
	
	

}
