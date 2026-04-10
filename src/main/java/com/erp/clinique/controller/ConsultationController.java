package com.erp.clinique.controller;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.erp.clinique.model.Consultation;
import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Medicament;
import com.erp.clinique.model.Ordonnance;
import com.erp.clinique.model.Prescription;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.ConsultationRepository;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.OrdonnanceRepository;
import com.erp.clinique.service.ConsultationService;
import com.erp.clinique.service.EmailService;
import com.erp.clinique.service.MedicamentService;
import com.erp.clinique.service.OrdonnanceService;
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
    private MedicamentService medicamentService;
    @Autowired
    private OrdonnanceRepository ordonnanceRepository;
    @Autowired
    private MedecinUserRepository medecinUserRepo;
    @Autowired
    private OrdonnanceService ordonnanceService;

    @Autowired
    private EmailService emailService; 

   
    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        
        MedecinUser medUser = medecinUserRepo.findByUserId(user.getId());
        if (medUser == null) {
            throw new RuntimeException("Médecin non trouvé pour cet utilisateur");
        }
        Long medecinId = medUser.getMedecinId();

        
        Pageable sortedByDateDesc = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Consultation> consultationPage;

        
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

        
        model.addAttribute("consultations", consultationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", consultationPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("date", date); 

        return "consultations/list";
    }

    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("consultation", new Consultation());
        model.addAttribute("rendezVousList", rendezVousService.findAll());
        return "consultations/form";
    }

   
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("consultation") Consultation consultation,
                       BindingResult result,
                       @RequestParam("rendezVousId") Long rendezVousId,
                       Model model,
                       RedirectAttributes redirectAttributes,
                       @RequestParam List<Long> medicamentId,
                       @RequestParam List<String> posologie,
                       @RequestParam List<String> duree,
                       @RequestParam List<Integer> quantite) {
    	
    	System.out.println(result.hasErrors());
        if (result.hasErrors()) {
            model.addAttribute("rendezVousList", rendezVousService.findAll());
            return "consultations/form";
        }
        
        RendezVous rv = rendezVousService.findById(rendezVousId)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous invalide avec l'ID : " + rendezVousId));
        
        consultation.setRendezVous(rv);
        consultationService.save(consultation);

        Ordonnance ordonnance = new Ordonnance();
        ordonnance.setConsultation(consultation);

        for (int i = 0; i < medicamentId.size(); i++) {
            Medicament medicament = medicamentService
                    .findById(medicamentId.get(i))
                    .orElseThrow();
         
            medicament.setqStock(medicament.getqStock() - quantite.get(i));
            Prescription p = new Prescription();
            p.setMedicament(medicament);
            p.setPosologie(posologie.get(i));
            p.setDuree(duree.get(i));
            p.setQuantite(quantite.get(i));
            p.setOrdonnance(ordonnance);
            ordonnance.getPrescriptions().add(p);
           // System.out.println("presci tafiditra");
        }

        ordonnanceRepository.save(ordonnance);
        
     
        try {
        	//String folder = "src/main/resources/static/pdf_ordonnances/";
        	//String folder = "uploads/pdf_ordonnances/";
        	String folder = "/app/pdf_ordonnances/";
            File dir = new File(folder);
            if (!dir.exists()) dir.mkdirs();

            String filename = "ordonnance_" + ordonnance.getId() + ".pdf";
            File pdfFile = new File(dir, filename);

            ordonnanceService.generateOrdonnancePdf(ordonnance, pdfFile);

            ordonnance.setPdfPath(folder + filename);
            ordonnanceRepository.save(ordonnance);
           
            String patientEmail = rv.getPatient().getEmail(); 
            String subject = "Votre ordonnance - Clinique";
            String body = "Bonjour,\n\n"
                    + "Voici votre ordonnance avec le détail des médicaments.\n"
                    + "Veuillez revenir à la secrétaire et lui communiquer votre numéro d'ordonnance "
                    + "pour pouvoir poursuivre l'achat des médicaments prescrits.\n\n"
                    + "Merci et bon rétablissement !";
            emailService.sendPdfEmail(patientEmail, subject, body, pdfFile);
            redirectAttributes.addFlashAttribute("success", "Consultation enregistrée et email envoyé !");
            
            rv.setStatut("TERMINE");
            rendezVousService.save(rv);
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Consultation enregistrée mais l'email n'a pas pu être envoyé.");
        }
        
        redirectAttributes.addFlashAttribute("success", "Consultation enregistrée !");
        return "redirect:/consultations";
    }

   
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

    // pour envoyer la liste des consultations au front
    @GetMapping("/api/list")
    @ResponseBody // Important pour renvoyer du JSON
    public Page<Consultation> getConsultationsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        // On reprend ta logique de récupération du médecin connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Users user = userService.findByUsername(username).orElseThrow();
        MedecinUser medUser = medecinUserRepo.findByUserId(user.getId());
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());
        
        if (keyword != null && !keyword.isEmpty()) {
            return consultationRepository.findByRendezVousMedecinIdAndRendezVousPatientNomContainingIgnoreCase(
                    medUser.getMedecinId(), keyword, pageable);
        }
        return consultationRepository.findByRendezVousMedecinId(medUser.getMedecinId(), pageable);
    }
}
