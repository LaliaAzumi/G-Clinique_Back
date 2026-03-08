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

import com.erp.clinique.model.Medicament;
import com.erp.clinique.repository.MedicamentRepository;
import com.erp.clinique.service.MedicamentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/medicaments")
public class MedicamentController {
	
	@Autowired
	private MedicamentRepository medicamentRepository;
	
	@Autowired
	private MedicamentService medicamentService;
	
	@GetMapping
    public String listmedicaments(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Medicament> medicamentPage;

        if (keyword != null && !keyword.isEmpty()) {
            medicamentPage = medicamentRepository.searchAll(keyword, pageable);
        } else {
            medicamentPage = medicamentRepository.findAll(pageable);
        }

        model.addAttribute("medicaments", medicamentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", medicamentPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "medicaments/list";
    }
    
    
   
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("medicament", new Medicament());
        return "medicaments/form";
    }

   
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("medicament") Medicament medicament,
                       BindingResult result,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (result.hasErrors()) {
            return "medicaments/form";
        }
        medicamentService.save(medicament);
        redirectAttributes.addFlashAttribute("success", "Medicament enregistre avec succes !");
        return "redirect:/medicaments";
       
        
    }

    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {
        return medicamentService.findById(id)
                .map(medicament -> {
                    model.addAttribute("medicament", medicament);
                    return "medicaments/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "medicaments non trouve !");
                    return "redirect:/medicaments";
                });
    }

    
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
        	medicamentService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "medicaments supprime avec succes !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer ce medicaments. Il a des rendez-vous associes.");
        }
        return "redirect:/medicaments";
    }

}
