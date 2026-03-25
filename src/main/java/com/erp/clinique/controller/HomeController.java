package com.erp.clinique.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.erp.clinique.model.Users;
import com.erp.clinique.service.ConsultationService;
import com.erp.clinique.service.MedecinService;
import com.erp.clinique.service.PatientService;
import com.erp.clinique.service.RendezVousService;
import com.erp.clinique.service.UserService;

@Controller
public class HomeController {
	@Autowired
	private UserService userService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private ConsultationService consultationService;
    @Autowired
    private MedecinService medecinService;
    @Autowired
    private RendezVousService rendezVousService;
    
    // Note: Le endpoint /login est géré par FastApiLoginController
    
    @GetMapping("/home")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        String username = auth.getName();
        Users user = userService.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<String> roles = auth.getAuthorities()
                                 .stream()
                                 .map(GrantedAuthority::getAuthority)
                                 .toList();
        int[] monthlyData = new int[12];
        List<Object[]> results = consultationService.countConsultationsByMonth();
        
        for (Object[] result : results) {
            int month = (int) result[0]; 
            long count = (long) result[1];
            monthlyData[month - 1] = (int) count;
        }
        
        model.addAttribute("monthlyConsultations", monthlyData);
        model.addAttribute("username", username);
        model.addAttribute("roles", roles);
        model.addAttribute("firstLogin", user.isFirstLogin());
        model.addAttribute("user", user); 
        model.addAttribute("totalPatients", patientService.getAllPatients().size());
        model.addAttribute("totalConsultations", consultationService.findAll().size());
        model.addAttribute("totalMedecins", medecinService.findAll().size());
        model.addAttribute("totalRdvAttente", rendezVousService.findByStatut("EN_ATTENTE").size());
        
        return "home"; 
    }
}