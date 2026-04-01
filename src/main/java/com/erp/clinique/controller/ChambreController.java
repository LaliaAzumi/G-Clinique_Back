package com.erp.clinique.controller;

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

import com.erp.clinique.model.Chambre;
import com.erp.clinique.repository.ChambreRepository;
import com.erp.clinique.service.ChambreService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/chambres")
public class ChambreController {

    @Autowired
    private ChambreService chambreService;

    @Autowired
    private ChambreRepository chambreRepository;

    /**
     * Liste les chambres avec pagination et recherche
     */
    @GetMapping
    public String listChambres(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Chambre> chambrePage;

        if (keyword != null && !keyword.isEmpty()) {
            // Assurez-vous d'avoir une méthode searchAll dans votre ChambreRepository
            chambrePage = chambreRepository.searchAll(keyword, pageable);
        } else {
            chambrePage = chambreRepository.findAll(pageable);
        }

        model.addAttribute("chambres", chambrePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", chambrePage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "chambres/list";
    }

    /**
     * Affiche le formulaire de création
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("chambre", new Chambre());
        return "chambres/form";
    }

    /**
     * Enregistre ou met à jour une chambre
     */
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("chambre") Chambre chambre,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "chambres/form";
        }

        chambreService.save(chambre);
        redirectAttributes.addFlashAttribute("success", "Chambre enregistrée avec succès !");
        
        return "redirect:/chambres";
    }

    /**
     * Affiche le formulaire de modification
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return chambreService.findById(id)
                .map(chambre -> {
                    model.addAttribute("chambre", chambre);
                    return "chambres/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Chambre non trouvée !");
                    return "redirect:/chambres";
                });
    }

    /**
     * Supprime une chambre
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            chambreService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Chambre supprimée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Impossible de supprimer cette chambre. Elle est peut-être occupée ou liée à une hospitalisation.");
        }
        return "redirect:/chambres";
    }
}