package com.erp.clinique.controller;

import com.erp.clinique.model.RendezVous;
import com.erp.clinique.service.MedecinService;
import com.erp.clinique.service.PatientService;
import com.erp.clinique.service.RendezVousService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rendezvous")
public class RendezVousController {

    @Autowired
    private RendezVousService rendezVousService;

    @Autowired
    private MedecinService medecinService;

    @Autowired
    private PatientService patientService;

    // Lister tous les rendez-vous
    @GetMapping
    public String list(Model model) {
        model.addAttribute("rendezVousList", rendezVousService.findAll());
        return "rendezvous/list";
    }

    // Afficher le formulaire de creation
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("rendezVous", new RendezVous());
        model.addAttribute("medecins", medecinService.findAll());
      //  model.addAttribute("patients", patientService.findAll());
        return "rendezvous/form";
    }

    // Enregistrer un nouveau rendez-vous
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("rendezVous") RendezVous rendezVous,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("medecins", medecinService.findAll());
      //      model.addAttribute("patients", patientService.findAll());
            return "rendezvous/form";
        }
        rendezVousService.save(rendezVous);
        redirectAttributes.addFlashAttribute("success", "Rendez-vous enregistre avec succes !");
        return "redirect:/rendezvous";
    }

    // Afficher le formulaire de modification
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return rendezVousService.findById(id)
                .map(rendezVous -> {
                    model.addAttribute("rendezVous", rendezVous);
                    model.addAttribute("medecins", medecinService.findAll());
                 //   model.addAttribute("patients", patientService.findAll());
                    return "rendezvous/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Rendez-vous non trouve !");
                    return "redirect:/rendezvous";
                });
    }

    // Supprimer un rendez-vous
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            rendezVousService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Rendez-vous supprime avec succes !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer ce rendez-vous. Il a une consultation associee.");
        }
        return "redirect:/rendezvous";
    }
}
