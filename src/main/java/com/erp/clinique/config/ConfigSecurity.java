package com.erp.clinique.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.erp.clinique.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

import com.erp.clinique.model.Medicament;
import com.erp.clinique.model.Users;
import com.erp.clinique.repository.MedicamentRepository;
import com.erp.clinique.service.UserAuthService;
import com.erp.clinique.service.UserService;

@Configuration
@EnableWebSecurity
public class ConfigSecurity {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/pdf_ordonnances/**").permitAll()
                .requestMatchers("/login", "/api/**", "/api/setup/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // nanao ajout any ito zah Stan
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().authenticated()
            )
            // .exceptionHandling(ex -> ex
            //     .authenticationEntryPoint((request, response, authException) -> {
            //         response.sendRedirect("/login");
            //     })
            // )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"error\": \"Unauthorized\"}");
                    } else {
                        response.sendRedirect("/login");
                    }
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8081"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            if (userService.findByUsername("admin").isEmpty()) {
                Users admin = new Users("admin", "admin@gmail.com", "admin123", "ADMIN");
                userService.saveUser(admin);
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
