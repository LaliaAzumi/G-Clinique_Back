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
import com.erp.clinique.service.UserService;

@Controller
public class HomeController {
	@Autowired
	private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login"; // correspond à login.html dans templates
    }

    @GetMapping("/home")
    public String home(Model model) {
    	// Récupère l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Nom d'utilisateur
        String username = auth.getName();
        // Récupère l'objet Users depuis la DB
        Users user = userService.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        
        
        // Liste des rôles
        List<String> roles = auth.getAuthorities()
                                 .stream()
                                 .map(GrantedAuthority::getAuthority)
                                 .toList();
        
        // Passe les infos à Thymeleaf
        model.addAttribute("username", username);
        model.addAttribute("roles", roles);
        // Vérifie si c'est le premier login
        model.addAttribute("firstLogin", user.isFirstLogin());
        model.addAttribute("user", user); // ← important pour th:field
        
        
        return "home"; // correspond à home.html dans templates
    }
}