package com.erp.clinique.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.UserRepository;
import com.erp.clinique.service.EmailService;
import com.erp.clinique.service.FastApiUserService;
import com.erp.clinique.service.UserService;
import com.erp.clinique.utils.MdpUtils;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private FastApiUserService fastApiUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; 
    
    @Autowired
    private MedecinUserRepository medecinUserRepository;
    @Autowired
    private UserRepository userRepository;
    

   
    @GetMapping("/secretaires")
    public String listSecretaires(
    		Model model,
    		 @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "5") int size,
             @RequestParam(required = false) String keyword
    ) {
    	Pageable pageable = PageRequest.of(page, size);
        Page<Users> secretairePage;

        if (keyword != null && !keyword.isEmpty()) {
            secretairePage = userRepository.searchByRole("SECRETAIRE", keyword, pageable);
        } else {
            secretairePage = userRepository.findByRole("SECRETAIRE", pageable);
        }

        model.addAttribute("secretaires", secretairePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", secretairePage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "secretaires/list";
        
    }

    
    @GetMapping("/secretaires/new")
    public String newSecretaire(Model model) {
        model.addAttribute("secretaire", new Users());
        return "secretaires/form"; 
    }

  
    @PostMapping("/secretaires/save")
    public String saveSecretaire(@Valid @ModelAttribute("user") Users user,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

       
        if (result.hasErrors()) {
            return "secretaires/form";
        }

        // Vérifie si l'utilisateur existe déjà via la base locale
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Nom d'utilisateur déjà utilisé !");
            return "secretaires/form";
        }

        // Génère un mot de passe aléatoire
        String randomPassword = MdpUtils.generateRandomMdp();

        // Crée l'utilisateur via le microservice FastAPI
        java.util.Map<String, Object> creationResult = fastApiUserService.createUser(
            user.getUsername(),
            user.getEmail(),
            randomPassword,
            "SECRETAIRE"
        );

        if (creationResult == null) {
            model.addAttribute("error", "Erreur lors de la création via le microservice. Vérifiez que FastAPI est démarré.");
            return "secretaires/form";
        }

        // Envoie l'email avec le mot de passe
        emailService.sendPasswordEmail(user.getEmail(), user.getUsername(), randomPassword);

        redirectAttributes.addFlashAttribute("success", "Secrétaire créé avec succès via le microservice !");
        return "redirect:/users/secretaires";
    }

    
    @PostMapping("/usersForM/save")
    public String saveUsersForM(@Valid @ModelAttribute("user") Users user,
                                 BindingResult result,
                                 Model model, 
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam Long medecinId) {

       
        if (result.hasErrors()) {
            model.addAttribute("medecinId", medecinId);
            return "medecinUser/form";
        }

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Nom d'utilisateur déjà utilisé !");
            model.addAttribute("medecinId", medecinId);
            return "medecinUser/form";
        }

        // Génère un mot de passe aléatoire
        String randomPassword = MdpUtils.generateRandomMdp();

        // Crée l'utilisateur via le microservice FastAPI
        java.util.Map<String, Object> userResult = fastApiUserService.createUser(
            user.getUsername(),
            user.getEmail(),
            randomPassword,
            "MEDECIN"
        );

        if (userResult == null) {
            model.addAttribute("error", "Erreur lors de la création via le microservice.");
            model.addAttribute("medecinId", medecinId);
            return "medecinUser/form";
        }

        // Récupère l'ID de l'utilisateur créé depuis la réponse FastAPI
        Long userId = null;
        if (userResult.containsKey("id")) {
            userId = Long.valueOf(userResult.get("id").toString());
        }

        // Crée le lien medecin-user si l'ID est disponible
        if (userId != null) {
            MedecinUser link = new MedecinUser(medecinId, userId);
            medecinUserRepository.save(link);
        }

        // Envoie l'email avec le mot de passe
        emailService.sendPasswordEmail(user.getEmail(), user.getUsername(), randomPassword);

        redirectAttributes.addFlashAttribute("success", "Médecin créé avec succès via le microservice !");
        return "redirect:/medecins";
    }

   
    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute("user") Users user) {
        Users dbUser = userService.findById(user.getId()).orElseThrow();
        dbUser.setMdp(user.getMdp());
        dbUser.setFirstLogin(false);
        userService.saveUser(dbUser);
        return "redirect:/home";
    }

    
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users/secretaires";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model,
                               RedirectAttributes redirectAttributes) {

        return userService.findById(id)   
                .map(user -> {
                    model.addAttribute("secretaire", user);
                    return "secretaires/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Secretaire non trouvé !");
                    return "redirect://users/secretaires";
                });
    }

    /**
     * Endpoint de test pour vérifier la connexion au microservice FastAPI
     * Affiche le statut de la connexion et le nombre d'utilisateurs
     */
    @GetMapping("/test-microservice")
    @ResponseBody
    public String testMicroserviceConnection() {
        java.util.Map<String, Object> result = fastApiUserService.listUsers(null);
        if (result == null) {
            return " Microservice FastAPI non disponible - Vérifiez que Python FastAPI tourne sur le port 8000";
        }
        int count = result.containsKey("count") ? ((Number) result.get("count")).intValue() : 0;
        return " Connexion au microservice FastAPI OK - " + count + " utilisateur(s) dans la base";
    }
}