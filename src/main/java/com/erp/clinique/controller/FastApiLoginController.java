package com.erp.clinique.controller;

import com.erp.clinique.service.FastApiAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Contrôleur de login qui utilise FastAPI pour l'authentification
 * Remplace l'authentification Spring Security par défaut
 */
@Controller
public class FastApiLoginController {

    @Autowired
    private FastApiAuthService fastApiAuthService;

    /**
     * Affiche la page de login
     */
    @GetMapping("/login")
    public String loginPage(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe invalide");
        }
        return "login"; // Retourne le template login.html
    }

    /**
     * Traite la soumission du formulaire de login
     * Appelle FastAPI pour authentifier et stocke le JWT dans un cookie
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpServletResponse response,
                               Model model) {
        // Appelle FastAPI pour authentifier
        String token = fastApiAuthService.authenticate(username, password);

        if (token != null) {
            // Crée un cookie avec le token JWT
            Cookie jwtCookie = new Cookie("jwt_token", token);
            jwtCookie.setHttpOnly(true); // Sécurité: inaccessible par JavaScript
            jwtCookie.setMaxAge(1800); // 30 minutes
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);

            // Redirige vers la page d'accueil
            return "redirect:/home";
        } else {
            // Authentification échouée
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe invalide");
            return "login";
        }
    }

    /**
     * Déconnexion: supprime le cookie JWT
     */
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt_token", null);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        return "redirect:/login?logout";
    }
}
