package com.erp.clinique.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.erp.clinique.model.Patient;
import com.erp.clinique.service.PatientService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

 // Lister tous les medecins
    @GetMapping
    public String list(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "patients/list";
    }
    
    // Afficher le formulaire de creation
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "patients/form";
    }
     // enregistre new client hehe
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("patient") Patient patient,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "patients/form";
        }
        System.out.println(patient.getId());
        System.out.println("ID reçu : " + patient.getId());

        if (patient.getId() != null) {
            // Modification d'un patient existant
            patientService.updatePatient(patient);
            redirectAttributes.addFlashAttribute("success", "Patient modifié avec succès !");
        } else {
            // Nouveau patient
            patientService.addPatient(patient);
            redirectAttributes.addFlashAttribute("success", "Patient enregistré avec succès !");
        }

        return "redirect:/patients";
    }
    
    // enregistre new client editer  hehe
    @PostMapping("/saveEdit/{id}")
    public String saveEdit(@PathVariable("id") Long id,
                           @Valid @ModelAttribute("patient") Patient patient,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "patients/form";
        }

        // On force l'ID du patient à partir de l'URL
        patient.setId(id);

        System.out.println("ID reçu via URL : " + patient.getId());

        // Modification d'un patient existant
        patientService.updatePatient(patient);
        redirectAttributes.addFlashAttribute("success", "Patient modifié avec succès !");

        return "redirect:/patients";
    }
    
    // Afficher le formulaire de modification
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {

        Optional<Patient> existingPatient = patientService.getPatientById(id);

        if (existingPatient.isPresent()) {
            // Ici tu passes le vrai patient avec son ID déjà rempli
        	System.out.println("Patient à modifier ID = " + existingPatient.get().getId());
            model.addAttribute("patient", existingPatient.get());
            return "patients/form";
        } else {
            redirectAttributes.addFlashAttribute("error", "Patient non trouvé !");
            return "redirect:/patients";
        }
    }
    
    // Supprimer un patient
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
