package com.erp.clinique.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.erp.clinique.model.Ordonnance;
import com.erp.clinique.repository.OrdonnanceRepository;

@Controller
@RequestMapping("/ordonnances")
public class OrdonnanceController {
	@GetMapping
    public String list() {
		return "ordonnances/list";
    }
	private final OrdonnanceRepository ordonnanceRepository;

    public OrdonnanceController(OrdonnanceRepository ordonnanceRepository) {
        this.ordonnanceRepository = ordonnanceRepository;
    }

    @PostMapping("/considerPaid/{id}")
    public String considerPaid(@PathVariable Long id, Model model) {
        Optional<Ordonnance> optionalOrd = ordonnanceRepository.findById(id);
        if(optionalOrd.isPresent()) {
            Ordonnance ord = optionalOrd.get();
            ord.setPaye(true); // met paye à true
            ordonnanceRepository.save(ord);
            model.addAttribute("ordonnance", ord); // remet l'ordonnance à jour
            model.addAttribute("successMessage", "Ordonnance marquée payée !");
            return "ordonnances/list"; // retourne la même vue Thymeleaf
        } else {
        	 model.addAttribute("errorMessage", "Ordonnance non trouvée");
             return "ordonnances/list"; // même vue mais avec erreur
        }
    }
 // Récupérer l'ordonnance par ID et afficher la page
    @PostMapping("/show")
    public String showOrdonnance(@RequestParam Long id, Model model) {
    	 Ordonnance ordonnance = ordonnanceRepository.findById(id).orElse(null);
    	    model.addAttribute("ordonnance", ordonnance);
        return "ordonnances/list"; // ton template Thymeleaf
    }
    
    @GetMapping("/Medecin")
    public String ordonnancesMedecin(@RequestParam(required = false) String patient, Model model) {
        List<Ordonnance> ordonnances;

        if(patient != null && !patient.isEmpty()) {
            // Rechercher uniquement les ordonnances du patient
            ordonnances = ordonnanceRepository.findByPatientName(patient);
        } else {
            // Par défaut, on ne montre **rien** au départ
            ordonnances = new ArrayList<>();
        }

        model.addAttribute("ordonnances", ordonnances);
        model.addAttribute("patient", patient);
        return "ordonnances/medecin";
    }
}
