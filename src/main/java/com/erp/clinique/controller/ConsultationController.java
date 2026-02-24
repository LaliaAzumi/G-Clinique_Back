package com.erp.clinique.controller;

import com.erp.clinique.model.Consultation;
import com.erp.clinique.service.ConsultationService;
import com.erp.clinique.service.RendezVousService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/consultations")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private RendezVousService rendezVousService;

    // Lister toutes les consultations
    @GetMapping
    public String list(Model model) {
        model.addAttribute("consultations", consultationService.findAll());
        return "consultations/list";
    }

    // Afficher le formulaire de creation
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("consultation", new Consultation());
        model.addAttribute("rendezVousList", rendezVousService.findAll());
        return "consultations/form";
    }

    // Enregistrer une nouvelle consultation
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("consultation") Consultation consultation,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("rendezVousList", rendezVousService.findAll());
            return "consultations/form";
        }
        consultationService.save(consultation);
        redirectAttributes.addFlashAttribute("success", "Consultation enregistree avec succes !");
        return "redirect:/consultations";
    }

    // Afficher le formulaire de modification
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return consultationService.findById(id)
                .map(consultation -> {
                    model.addAttribute("consultation", consultation);
                    model.addAttribute("rendezVousList", rendezVousService.findAll());
                    return "consultations/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Consultation non trouvee !");
                    return "redirect:/consultations";
                });
    }

    // Supprimer une consultation
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            consultationService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Consultation supprimee avec succes !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression.");
        }
        return "redirect:/consultations";
    }
}
