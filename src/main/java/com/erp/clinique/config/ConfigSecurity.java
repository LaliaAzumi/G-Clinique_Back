package com.erp.clinique.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.erp.clinique.model.Users;
import com.erp.clinique.service.UserAuthService;
import com.erp.clinique.service.UserService;

@Configuration
@EnableWebSecurity
public class ConfigSecurity {
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserAuthService userAuthService) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN") // pages admin
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .userDetailsService(userAuthService);
            ;
        return http.build();
    }

   

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            if (userService.findByUsername("admin").isEmpty()) {
                Users admin = new Users("admin", "nekenarakotomalala760@gmail.com", "admin123", "ADMIN");
                userService.saveUser(admin);
                
                
            }
        };
    }
}
