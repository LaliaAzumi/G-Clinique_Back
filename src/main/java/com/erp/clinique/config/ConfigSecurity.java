package com.erp.clinique.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.erp.clinique.model.ActeMedical;
import com.erp.clinique.model.Medicament;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.ActeMedicalRepository;
import com.erp.clinique.repository.MedicamentRepository;
import com.erp.clinique.security.JwtAuthenticationFilter;
import com.erp.clinique.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class ConfigSecurity {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
	
    @Bean
   
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        // Désactiver CORS si vous gérez déjà cela au niveau du Controller ou de FastAPI
        .cors(cors -> cors.configure(http)) 
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Routes statiques et login
            .requestMatchers("/api/v1/patients/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/api/v1/rendez-vous/**").permitAll() // Ou .permitAll() pour tester
            .requestMatchers("/css/**", "/js/**", "/images/**", "/pdf_ordonnances/**").permitAll()
            .requestMatchers("/login").permitAll()
            // Routes API : On autorise explicitement
            .requestMatchers("/api/v1/auth/**", "/api/setup/**").permitAll()
            .requestMatchers("/api/v1/auth/verify").permitAll()
            .requestMatchers("/api/**").permitAll() 
            // Admin
            .requestMatchers("/admin/**").hasRole("ADMIN") 
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
                // Si la requête commence par /api, on renvoie 401 au lieu de rediriger vers /login
                if (request.getRequestURI().startsWith("/api/")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    response.sendRedirect("/login");
                }
            })
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    CommandLineRunner initAdmin(UserService userService, ActeMedicalRepository acteRepo) {
        return args -> {
            if (userService.findByUsername("admin").isEmpty()) {
                Users admin = new Users("admin", "admin@gmail.com", "admin123", "ADMIN");
                userService.saveUser(admin);
            }
         // 2. Initialisation des Actes Médicaux
            if (acteRepo.count() == 0) {
            	acteRepo.save(new ActeMedical("Consultation Générale", 25000.0));
                acteRepo.save(new ActeMedical("Analyse de sang", 15000.0));
                acteRepo.save(new ActeMedical("Radiographie", 45000.0));
                acteRepo.save(new ActeMedical("Échographie Abdominale", 60000.0));
                acteRepo.save(new ActeMedical("Électrocardiogramme (ECG)", 35000.0));
                acteRepo.save(new ActeMedical("Scanner", 120000.0));
                System.out.println("✅ Actes médicaux de base créés");
            }
        };
    } 
    
        @Bean
        CommandLineRunner initDatabase(MedicamentRepository repository) {
            return args -> {
                if (repository.count() == 0) {
                    System.out.println("--- Initialisation de la pharmacie ---");
                    repository.save(new Medicament("Paracétamol", "Antalgique et antipyrétique", 500, 100));
                    repository.save(new Medicament("Ibuprofène", "Anti-inflammatoire non stéroïdien", 600, 80));
                    repository.save(new Medicament("Amoxicilline", "Antibiotique pénicilline", 1200, 50));
                    repository.save(new Medicament("Aspirine", "Antalgique et anti-inflammatoire", 300, 120));
                    repository.save(new Medicament("Metformine", "Traitement du diabète", 800, 60));
                    repository.save(new Medicament("Oméprazole", "Inhibiteur de la pompe à protons", 700, 40));
                    repository.save(new Medicament("Céfuroxime", "Antibiotique céphalosporine", 1500, 30));
                    repository.save(new Medicament("Clopidogrel", "Antiagrégant plaquettaire", 900, 45));
                    repository.save(new Medicament("Salbutamol", "Bronchodilatateur", 650, 70));
                    repository.save(new Medicament("Prednisone", "Corticostéroïde", 550, 35));
                    repository.save(new Medicament("Loratadine", "Antihistaminique", 400, 90));
                    repository.save(new Medicament("Amlodipine", "Antihypertenseur", 750, 55));
                    repository.save(new Medicament("Atorvastatine", "Hypolipémiant", 1100, 50));
                    repository.save(new Medicament("Ciprofloxacine", "Antibiotique fluoroquinolone", 1300, 40));
                    repository.save(new Medicament("Doxycycline", "Antibiotique tétracycline", 1250, 60));
                    repository.save(new Medicament("Furosemide", "Diurétique", 500, 70));
                    repository.save(new Medicament("Hydrocortisone", "Corticostéroïde topique", 450, 30));
                    repository.save(new Medicament("Nitroglycérine", "Vasodilatateur", 950, 25));
                    repository.save(new Medicament("Ranitidine", "Anti-H2 pour estomac", 600, 50));
                    repository.save(new Medicament("Metoprolol", "Bêta-bloquant", 700, 60));

                    System.out.println("--- " + repository.count() + " médicaments insérés ! ---");
                }
            };
        }
}
