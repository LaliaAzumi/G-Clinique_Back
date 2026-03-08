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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.erp.clinique.model.MedecinUser;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.MedecinUserRepository;
import com.erp.clinique.repository.UserRepository;
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
                                 Model model) {

       
        if (result.hasErrors()) {
            return "secretaires/form";
        }

      
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Nom d'utilisateur déjà utilisé !");
            return "secretaires/form";
        }

        
        String randomPassword = MdpUtils.generateRandomMdp();
       // System.out.println("Mdp huhu " + randomPassword); 

        user.setMdp(randomPassword);
        user.setRole("SECRETAIRE");
        user.setFirstLogin(true);
        userService.saveUser(user);
        emailService.sendPasswordEmail(user.getEmail(), user.getUsername(),randomPassword);

        return "redirect:/users/secretaires";
    }

    
    @PostMapping("/usersForM/save")
    public String saveUsersForM(@Valid @ModelAttribute("user") Users user,
                                 BindingResult result,
                                 Model model, 
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam Long medecinId) {

       
        if (result.hasErrors()) {
            return "secretaires/form";
        }

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "Nom d'utilisateur déjà utilisé !");
            return "medecinUser/form";
        }

        String randomPassword = MdpUtils.generateRandomMdp();
        //System.out.println("Mdp huhu : " + randomPassword);

        user.setMdp(randomPassword);
        user.setRole("MEDECIN");
        user.setFirstLogin(true);

        Users userEnregistre = userService.saveUser(user);
        MedecinUser link = new MedecinUser(medecinId, userEnregistre.getId());
        medecinUserRepository.save(link);
        emailService.sendPasswordEmail(user.getEmail(), user.getUsername(),randomPassword);
        redirectAttributes.addFlashAttribute("success", "Medecin enregistre avec succes !");
        
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
}