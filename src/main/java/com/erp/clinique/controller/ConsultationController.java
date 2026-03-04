package com.erp.clinique.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import com.erp.clinique.model.Consultation;
import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.ConsultationRepository;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.service.ConsultationService;
import com.erp.clinique.service.RendezVousService;
import com.erp.clinique.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/consultations")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private RendezVousService rendezVousService;
    @Autowired
    private ConsultationRepository consultationRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private MedecinUserRepository medecinUserRepo;

    // Lister toutes les consultations
    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // 1️⃣ Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 2️⃣ Récupérer le medecinId correspondant
        MedecinUser medUser = medecinUserRepo.findByUserId(user.getId());
        if (medUser == null) {
            throw new RuntimeException("Médecin non trouvé pour cet utilisateur");
        }
        Long medecinId = medUser.getMedecinId();

        // 3️⃣ Pagination + tri DESC
        Pageable sortedByDateDesc = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Consultation> consultationPage;

        // 4️⃣ Logique de recherche
        if ((keyword != null && !keyword.isEmpty()) && date != null) {
            consultationPage = consultationRepository
                .findByRendezVousMedecinIdAndRendezVousPatientNomContainingIgnoreCaseAndDate(
                    medecinId, keyword, date, sortedByDateDesc);
        } else if (keyword != null && !keyword.isEmpty()) {
            consultationPage = consultationRepository
                .findByRendezVousMedecinIdAndRendezVousPatientNomContainingIgnoreCase(
                    medecinId, keyword, sortedByDateDesc);
        } else if (date != null) {
            consultationPage = consultationRepository
                .findByRendezVousMedecinIdAndDate(medecinId, date, sortedByDateDesc);
        } else {
            consultationPage = consultationRepository
                .findByRendezVousMedecinId(medecinId, sortedByDateDesc);
        }

        // 5️⃣ Passer les données au modèle
        model.addAttribute("consultations", consultationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", consultationPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("date", date); // pour pré-remplir le champ date dans le formulaire

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
        Long rvId = consultation.getRendezVous().getId();
        RendezVous rv = rendezVousService.findById(rvId).get();
        consultation.setRendezVous(rv);

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
