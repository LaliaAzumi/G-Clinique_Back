package com.erp.clinique.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Medicament;
import com.erp.clinique.model.RendezVous;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.MedicamentRepository;
import com.erp.clinique.service.RendezVousService;
import com.erp.clinique.service.UserService;

@Controller
@RequestMapping("/callendar")
public class CallendarController {
	@Autowired
    private RendezVousService rendezVousService;

    @Autowired
    private UserService userService;

    @Autowired
    private MedecinUserRepository medecinUserRepo;
    @Autowired
    private MedicamentRepository medicamentRepository;
	// Lister tous les rendez-vous
    
	@GetMapping("/callendars")
	public String agendaSemaine(@RequestParam(required = false) String startOfWeek, Model model) {

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

        // 3️⃣ Calcul semaine
        LocalDate start;
        if (startOfWeek != null) {
            start = LocalDate.parse(startOfWeek);
        } else {
            start = LocalDate.now().with(DayOfWeek.MONDAY);
        }
        LocalDate end = start.plusDays(6);

        // 4️⃣ Récupérer les rendez-vous du médecin pour la semaine
        List<RendezVous> rdvs = rendezVousService.findByMedecinIdAndDateBetween(medecinId, start, end);
        
        List<Medicament> medicaments = medicamentRepository.findAll();
        model.addAttribute("medicaments", medicaments);

        // 5️⃣ Passer au modèle pour Thymeleaf
        model.addAttribute("startOfWeek", start);
        model.addAttribute("endOfWeek", end);
        model.addAttribute("rendezVousList", rdvs);

        return "agenda/list";
    }
	
	@GetMapping
	public String redirectToCalendars() {
	    return "redirect:/callendar/callendars";
	}

}
