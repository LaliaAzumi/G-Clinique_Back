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
import com.erp.clinique.service.NotificationService;
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
    @Autowired
    private NotificationService notificationService;
    
    

   
    @GetMapping
    public String list(	Model model ,
			            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,@RequestParam(required = false) String keyword,
			            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			            @RequestParam(required = false) String statut
			            ) {   
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

    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("rendezVous", new RendezVous());
        model.addAttribute("medecins", medecinService.findAll());
        model.addAttribute("patients", patientService.getAllPatients());
        return "rendezvous/form";
    }

   
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
    
        List<LocalTime> heuresLibres = rendezVousService.getHeuresDisponibles(
                rendezVous.getMedecin().getId(), rendezVous.getDate());

       
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

       
        rendezVousService.save(rendezVous);

        
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
                //System.out.println("Email médecin : " + medecinEmail);
                //notif
                String notifMessage = String.format(
                        "Nouveau rendez-vous pour %s %s le %s à %s",
                        rendezVous.getPatient().getNom(),
                        rendezVous.getPatient().getPrenom(),
                        rendezVous.getDate(),
                        rendezVous.getHeure()
                );
                Long idmed = rendezVous.getMedecin().getId();
                Optional<MedecinUser> idusers = medecinUserRepository.findByMedecinId(idmed);
                
                MedecinUser muy = idusers.get();
                System.out.println("MedecinUser trouvé : userId=" + muy.getUserId());
                
                notificationService.sendNotificationToUser(muy.getUserId(), notifMessage);
            } else {
                System.out.println("Utilisateur associé au MedecinUser introuvable !");
            }
        } else {
            System.out.println("Aucun MedecinUser trouvé pour ce médecin !");
        }

        redirectAttributes.addFlashAttribute("success", "Rendez-vous enregistré et notifications envoyées !");
        return "redirect:/rendezvous";
      
    }

    
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
        
    
        String notifMessage;
        String emailMessage; 

        if ("ANNULE".equalsIgnoreCase(statut)) {
            notifMessage = String.format(
                "Le rendez-vous avec le motif %s de %s %s prévu le %s à %s a été annulé.",
                rv.getMotif(),
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getHeure()
            );
            emailMessage = String.format(
                "Bonjour %s %s,\n\nVotre rendez-vous prévu le %s à %s pour le motif %s a été annulé.\n\nCordialement,\nL'équipe ERP Clinique",
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getHeure(),
                rv.getMotif()
            );
        } else if (!rv.getDate().equals(date) && !rv.getHeure().equals(heure)) {
            notifMessage = String.format(
                "Le rendez-vous avec le motif %s de %s %s a été déplacé du %s à %s vers le %s à %s.",
                rv.getMotif(),
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getHeure(),
                date,
                heure
            );
            emailMessage = String.format(
                "Bonjour %s %s,\n\nVotre rendez-vous prévu le %s à %s pour le motif %s a été déplacé au %s à %s.\n\nCordialement,\nL'équipe ERP Clinique",
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getHeure(),
                rv.getMotif(),
                date,
                heure
            );
        } else if (!rv.getDate().equals(date)) {
            notifMessage = String.format(
                "Le rendez-vous avec le motif %s de %s %s prévu le %s a été déplacé à la date %s.",
                rv.getMotif(),
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                date
            );
            emailMessage = String.format(
                "Bonjour %s %s,\n\nVotre rendez-vous prévu le %s pour le motif %s a été déplacé au %s.\n\nCordialement,\nL'équipe ERP Clinique",
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getMotif(),
                date
            );
        } else if (!rv.getHeure().equals(heure)) {
            notifMessage = String.format(
                "Le rendez-vous avec le motif %s de %s %s prévu le %s à %s a été déplacé à %s.",
                rv.getMotif(),
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getHeure(),
                heure
            );
            emailMessage = String.format(
                "Bonjour %s %s,\n\nVotre rendez-vous prévu le %s à %s pour le motif %s a été déplacé à %s.\n\nCordialement,\nL'équipe ERP Clinique",
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getHeure(),
                rv.getMotif(),
                heure
            );
        } else {
            notifMessage = String.format(
                "Le rendez-vous avec le motif %s de %s %s prévu le %s à %s a été modifié.",
                rv.getMotif(),
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getHeure()
            );
            emailMessage = String.format(
                "Bonjour %s %s,\n\nVotre rendez-vous prévu le %s à %s pour le motif %s a été modifié.\n\nCordialement,\nL'équipe ERP Clinique",
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                rv.getDate(),
                rv.getHeure(),
                rv.getMotif()
            );
        }

      
        Long idmed = rv.getMedecin().getId();
        Optional<MedecinUser> idusers = medecinUserRepository.findByMedecinId(idmed);
        if (idusers.isPresent()) {
            MedecinUser muy = idusers.get();
            System.out.println("MedecinUser trouvé : userId=" + muy.getUserId());
            notificationService.sendNotificationToUser(muy.getUserId(), notifMessage);
        }

        
        String patientEmail = rv.getPatient().getEmail();
        String sujet = "Modification de votre rendez-vous";
        emailService.sendRendezVousEmail(patientEmail, sujet, emailMessage);

        
        rv.setDate(date);
        rv.setHeure(heure);
        rv.setStatut(statut);                
        rendezVousService.save(rv);
        
        return "redirect:/rendezvous"; 
    }
    
    
    @PostMapping("/updateStatusM")
    public String updateRendezVousM(@RequestParam Long id,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
                                   @RequestParam String statut) {
        RendezVous rv = rendezVousService.findById(id)
                        .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));
        
        String notifMessage = String.format(
                "Le rendez-vous avec %s %s prévu le %s à %s a été reporté. Merci de le reprogrammer.",
                rv.getPatient().getNom(),
                rv.getPatient().getPrenom(),
                date,
                heure
        );
        
        rv.setDate(date);
        rv.setHeure(heure);
        rv.setStatut(statut);                
        rendezVousService.save(rv);
        
        List<Users> secretaires = userService.findByRole("SECRETAIRE");
        
        for (Users sec : secretaires) {
                notificationService.sendNotificationToUser(sec.getId(), notifMessage);
        }
        
        return "redirect:/callendar/callendars"; 
    }
   
}
