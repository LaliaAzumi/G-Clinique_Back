package com.erp.clinique.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.erp.clinique.model.Users;
import com.erp.clinique.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users saveUser(Users user) {
        user.setMdp(passwordEncoder.encode(user.getMdp()));
        return userRepository.save(user);
    }

    /**
     * Sauvegarde un utilisateur avec mot de passe deja hache (appele par FastAPI)
     * Ne re-hache pas le mot de passe
     */
    public Users saveUserInternal(Users user) {
        // Le mot de passe est deja hache par FastAPI, on sauvegarde directement
        return userRepository.save(user);
    }

    public List<Users> findAll() {
        return userRepository.findAll();
    }

    public Optional<Users> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public List<Users> findByRole(String role) {
        return userRepository.findByRole(role);
    }
    public Optional<Users> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
    public Users save(Users user) {
        return userRepository.save(user); // C'est ICI que la magie JPA opère
    }
    
    public Optional<Users> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
}