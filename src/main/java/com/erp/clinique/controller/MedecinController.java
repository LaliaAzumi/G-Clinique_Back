package com.erp.clinique.controller;

import com.erp.clinique.repository.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.erp.clinique.model.Medecin;
import com.erp.clinique.model.Users;
import com.erp.clinique.service.MedecinService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/medecins")
public class MedecinController {

    @Autowired
    private MedecinService medecinService;
    @Autowired
    private MedecinRepository medecinRepository;
    
    // Lister tous les medecins
    @GetMapping
    public String listMedecins(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Medecin> medecinPage;

        if (keyword != null && !keyword.isEmpty()) {
            medecinPage = medecinRepository.searchAll(keyword, pageable);
        } else {
            medecinPage = medecinRepository.findAll(pageable);
        }

        model.addAttribute("medecins", medecinPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", medecinPage.getTotalPages());
        model.addAttribute("keyword", keyword);

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
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (result.hasErrors()) {
            return "medecins/form";
        }
       // medecinService.save(medecin);
        //redirectAttributes.addFlashAttribute("success", "Medecin enregistre avec succes !");
        // return "redirect:/medecins";
        Medecin medecinEnregistre = medecinService.save(medecin);
        
 
        model.addAttribute("user", new Users());
        model.addAttribute("medecinId", medecinEnregistre.getId());

        return "medecinUser/form"; // ta page Thymeleaf pour entrer username/email
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
