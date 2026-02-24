package com.erp.clinique.controller;

import com.erp.clinique.model.Medecin;
import com.erp.clinique.service.MedecinService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medecins")
public class MedecinController {

    @Autowired
    private MedecinService medecinService;

    // Lister tous les medecins
    @GetMapping
    public String list(Model model) {
        model.addAttribute("medecins", medecinService.findAll());
        return "medecins/list";
    }

    // Afficher le formulaire de creation
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("medecin", new Medecin());
        return "medecins/form";
    }

    // Enregistrer un nouveau medecin
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("medecin") Medecin medecin,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "medecins/form";
        }
        medecinService.save(medecin);
        redirectAttributes.addFlashAttribute("success", "Medecin enregistre avec succes !");
        return "redirect:/medecins";
    }

    // Afficher le formulaire de modification
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return medecinService.findById(id)
                .map(medecin -> {
                    model.addAttribute("medecin", medecin);
                    return "medecins/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Medecin non trouve !");
                    return "redirect:/medecins";
                });
    }

    // Supprimer un medecin
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            medecinService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Medecin supprime avec succes !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer ce medecin. Il a des rendez-vous associes.");
        }
        return "redirect:/medecins";
    }
}
