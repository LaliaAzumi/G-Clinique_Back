package com.erp.clinique.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.erp.clinique.model.Medecin;
import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.RendezVousRepository;
import com.erp.clinique.service.EmailService;
import com.erp.clinique.service.MedecinService;
import com.erp.clinique.service.PatientService;
import com.erp.clinique.service.RendezVousService;
import com.erp.clinique.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/rendezvous")
public class RendezVousController {

    @Autowired
    private RendezVousService rendezVousService;
    @Autowired
    private MedecinService medecinService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private MedecinUserRepository medecinUserRepository;
    @Autowired
    private RendezVousRepository rendezvousRepository;
    
    

    // Lister tous les rendez-vous
    @GetMapping
    public String list(	Model model ,
			            @RequestParam(defaultValue = "0") int page,
			            @RequestParam(defaultValue = "5") int size,
			            
			            @RequestParam(required = false) String keyword,
			            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			            @RequestParam(required = false) String statut
			            ) {
			            
			            // Logique pour filtrer 
			            List<RendezVous> list;
			            
			            if ((keyword != null && !keyword.isEmpty()) || date != null || (statut != null && !statut.isEmpty())) {
			                list = rendezVousService.search(keyword, date, statut);
			            } else {
			                list = rendezVousService.findAll();
			            }

			           
    	Pageable pageable = PageRequest.of(page, size);

        Page<RendezVous> rendezvousPage;

        if (keyword != null && !keyword.isEmpty()) {
            rendezvousPage = rendezvousRepository.searchAll(keyword, pageable);
        } else {
            rendezvousPage = rendezvousRepository.findAll(pageable);
        }
        model.addAttribute("rendezVousList", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("date", date);
        model.addAttribute("statut", statut);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rendezvousPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "rendezvous/list";
    }

    // Afficher le formulaire de creation
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("rendezVous", new RendezVous());
        model.addAttribute("medecins", medecinService.findAll());
        model.addAttribute("patients", patientService.getAllPatients());
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
            model.addAttribute("patients", patientService.getAllPatients());
            return "rendezvous/form";
        }
        
        
        boolean isNew = (rendezVous.getId() == null);
     // 1. Récupérer les heures disponibles
        List<LocalTime> heuresLibres = rendezVousService.getHeuresDisponibles(
                rendezVous.getMedecin().getId(), rendezVous.getDate());

        // 2. Vérifier si l'heure choisie est libre
        if (!heuresLibres.contains(rendezVous.getHeure())) {
        	String message;
            if (heuresLibres.isEmpty()) {
                message = "Pas de créneaux disponibles ce jour-là. Veuillez choisir une autre date.";
            } else {
                message = "Ce créneau n'est pas disponible. Heures libres : " + heuresLibres;
            }
            result.rejectValue("heure", "error.rendezVous", message
                    );
            model.addAttribute("medecins", medecinService.findAll());
            model.addAttribute("patients", patientService.getAllPatients());
            return "rendezvous/form";
        }

        //enregisterna apina condition 
        rendezVousService.save(rendezVous);

        // Email patient
        String patientEmail = rendezVous.getPatient().getEmail();
        System.out.println("Email patient : " + patientEmail);

        Medecin medecin = rendezVous.getMedecin();
        System.out.println("Médecin associé au RDV : " + medecin.getNom() + " (ID=" + medecin.getId() + ")");

        Optional<MedecinUser> muOpt = medecinUserRepository.findByMedecinId(medecin.getId());
        if (muOpt.isPresent()) {
            MedecinUser mu = muOpt.get();
            System.out.println("MedecinUser trouvé : userId=" + mu.getUserId());

            Optional<Users> userOpt = userService.findById(mu.getUserId());
            if (userOpt.isPresent()) {
                String medecinEmail = userOpt.get().getEmail();
                System.out.println("Email médecin : " + medecinEmail);

                String sujet = isNew ? "Nouveau rendez-vous" : "Rendez-vous modifié";
                String corps = String.format(
                    "Bonjour,\n\nLe rendez-vous pour  %s %s est %s.\nDate : %s\nHeure : %s\nStatut : %s",
                    rendezVous.getPatient().getNom(),
                    rendezVous.getPatient().getPrenom(),

                    isNew ? "créé" : "mis à jour",
                    rendezVous.getDate(),
                    rendezVous.getHeure(),
                    rendezVous.getStatut()
                );

                emailService.sendRendezVousEmail(patientEmail, sujet, corps);
                emailService.sendRendezVousEmail(medecinEmail, sujet, corps);
            } else {
                System.out.println("Utilisateur associé au MedecinUser introuvable !");
            }
        } else {
            System.out.println("Aucun MedecinUser trouvé pour ce médecin !");
        }

        redirectAttributes.addFlashAttribute("success", "Rendez-vous enregistré et notifications envoyées !");
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
                    model.addAttribute("patients", patientService.getAllPatients());
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
    @PostMapping("/updateStatus")
    public String updateRendezVous(@RequestParam Long id,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
                                   @RequestParam String statut) {
        RendezVous rv = rendezVousService.findById(id)
                        .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));
        rv.setDate(date);
        rv.setHeure(heure);
        rv.setStatut(statut);                
        rendezVousService.save(rv);
        return "redirect:/callendar/callendars";
    }
   
}
