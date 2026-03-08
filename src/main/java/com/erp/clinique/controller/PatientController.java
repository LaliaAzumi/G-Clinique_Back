package com.erp.clinique.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.erp.clinique.model.Patient;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.repository.PatientRepository;
import com.erp.clinique.service.MedecinService;
import com.erp.clinique.service.PatientService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    private MedecinService medecinService;
    @Autowired
    private PatientRepository patientRepository;


    @GetMapping
    public String listPatients(Model model,
    		  @RequestParam(defaultValue = "0") int page,
              @RequestParam(defaultValue = "5") int size,
              @RequestParam(required = false) String keyword) {
    	 Pageable pageable = PageRequest.of(page, size);

         Page<Patient> patientPage;

         if (keyword != null && !keyword.isEmpty()) {
             patientPage = patientRepository.searchAll(keyword, pageable);
         } else {
             patientPage = patientRepository.findAll(pageable);
         }

         model.addAttribute("patients", patientPage.getContent());
         model.addAttribute("currentPage", page);
         model.addAttribute("totalPages", patientPage.getTotalPages());
         model.addAttribute("keyword", keyword);

         return "patients/list";
    }
    
   
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patients/form";
    }
     
    
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("patient") Patient patient,
                       BindingResult result,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (result.hasErrors()) {
            return "patients/form";
        }
        System.out.println(patient.getId());
        System.out.println("ID reçu : " + patient.getId());

        if (patient.getId() != null) {
            patientService.updatePatient(patient);
            redirectAttributes.addFlashAttribute("success", "Patient modifié avec succès !");
        } else {
        	
        	if (patientService.existsByEmail(patient.getEmail())) {
        	    redirectAttributes.addFlashAttribute("error", "Patient déjà existant !");
        	
                model.addAttribute("rendezVous", new RendezVous());
                model.addAttribute("medecins", medecinService.findAll());
                model.addAttribute("patients", patientService.getAllPatients());

               
                Patient existingPatient = patientService.getByEmail(patient.getEmail());
                model.addAttribute("selectedPatient", existingPatient);

             
                model.addAttribute("error", "Patient déjà existant !");
                return "rendezvous/form";
        	    
        	} else {
        	    patientService.addPatient(patient);
        	    redirectAttributes.addFlashAttribute("success", "Patient enregistré avec succès !");
        	    return "redirect:/patients";
        	}
        }

        return "redirect:/patients";
    }
    
   
    @PostMapping("/saveEdit/{id}")
    public String saveEdit(@PathVariable("id") Long id,
                           @Valid @ModelAttribute("patient") Patient patient,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "patients/form";
        }

        
        patient.setId(id);

       // System.out.println("id url" + patient.getId());

        patientService.updatePatient(patient);
        redirectAttributes.addFlashAttribute("success", "Patient modifié avec succès !");

        return "redirect:/patients";
    }
    
  
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {

        Optional<Patient> existingPatient = patientService.getPatientById(id);

        if (existingPatient.isPresent()) {
     
        	System.out.println("Patient à modifier ID = " + existingPatient.get().getId());
            model.addAttribute("patient", existingPatient.get());
            return "patients/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Patient non trouvé !");
            return "redirect:/patients";
        }
    }
    
    
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
        	patientService.deletePatientById(id);
            redirectAttributes.addFlashAttribute("success", "patient supprime avec succes !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer ce patient. Il a des rendez-vous associes.");
        }
        return "redirect:/patients";
    }

    
   
    
   
    
}
