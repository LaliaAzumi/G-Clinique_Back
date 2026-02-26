package com.erp.clinique.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.service.EmailService;
import com.erp.clinique.service.UserService;
import com.erp.clinique.utils.MdpUtils;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; 
    
    @Autowired
    private MedecinUserRepository medecinUserRepository;
    

    // Liste des utilisateurs selon rôle
    @GetMapping("/secretaires")
    public String listSecretaires(Model model) {
        List<Users> secretaires = userService.findByRole("SECRETAIRE");
        model.addAttribute("secretaires", secretaires);
        return "secretaires/list"; // Thymeleaf template
    }

    // Formulaire création secrétaire
    @GetMapping("/secretaires/new")
    public String newSecretaire(Model model) {
        model.addAttribute("secretaire", new Users());
        return "secretaires/form"; // Thymeleaf template
    }

    // Sauvegarde secrétaire
    @PostMapping("/secretaires/save")
    public String saveSecretaire(@Valid @ModelAttribute("user") Users user,
                                 BindingResult result,
                                 Model model) {

        // Validation côté serveur
        if (result.hasErrors()) {
            return "secretaires/form";
        }

        // Vérifier si username existe
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Nom d'utilisateur déjà utilisé !");
            return "secretaires/form";
        }

        // Générer mot de passe aléatoire
        String randomPassword = MdpUtils.generateRandomMdp();
        System.out.println("Mot de passe généré : " + randomPassword); // debug

        // Encoder le mot de passe
        user.setMdp(randomPassword);

        // Role SECRETAIRE
        user.setRole("SECRETAIRE");

        // Premiers login obligatoire changement mdp
        user.setFirstLogin(true);

        // Sauvegarder en base
        userService.saveUser(user);

        // Envoyer email avec mot de passe temporaire
        emailService.sendPasswordEmail(user.getEmail(), user.getUsername(),randomPassword);

        return "redirect:/users/secretaires";
    }

    // Sauvegarde users for medecin
    @PostMapping("/usersForM/save")
    public String saveUsersForM(@Valid @ModelAttribute("user") Users user,
                                 BindingResult result,
                                 Model model, 
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam Long medecinId) {

        // Validation côté serveur
        if (result.hasErrors()) {
            return "secretaires/form";
        }

        // Vérifier si username existe
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Nom d'utilisateur déjà utilisé !");
            return "medecinUser/form";
        }

        // Générer mot de passe aléatoire
        String randomPassword = MdpUtils.generateRandomMdp();
        System.out.println("Mot de passe généré : " + randomPassword); // debug

        // Encoder le mot de passe
        user.setMdp(randomPassword);

        // Role MEDECIN
        user.setRole("MEDECIN");

        // Premiers login obligatoire changement mdp
        user.setFirstLogin(true);

        // Sauvegarder en base
       // userService.saveUser(user);
        Users userEnregistre = userService.saveUser(user);
        
     // Créer le lien dans MedecinUser
        MedecinUser link = new MedecinUser(medecinId, userEnregistre.getId());
        medecinUserRepository.save(link);

        // Envoyer email avec mot de passe temporaire
        emailService.sendPasswordEmail(user.getEmail(), user.getUsername(),randomPassword);

        redirectAttributes.addFlashAttribute("success", "Medecin enregistre avec succes !");
        
        return "redirect:/medecins";
    }

    // Sauvegarder nouveau mot de passe
    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute("user") Users user) {
        Users dbUser = userService.findById(user.getId()).orElseThrow();
        dbUser.setMdp(user.getMdp());
        dbUser.setFirstLogin(false);
        userService.saveUser(dbUser);
        return "redirect:/home";
    }

    // Supprimer un user (optionnel)
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users/secretaires";
    }
}